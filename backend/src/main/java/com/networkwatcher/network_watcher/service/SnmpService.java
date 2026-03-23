package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.Device;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SnmpService {
    @Autowired
    private DeviceService deviceService;

    public record SnmpOctetCounters(long inOctets, long outOctets) {}

    public boolean enrichDevice(String ipAddress, String community) {
        try {
            Address targetAddress = GenericAddress.parse("udp:" + ipAddress + "/161");
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            transport.listen();

            Snmp snmp = new Snmp(transport);

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setRetries(1);
            target.setTimeout(1500);
            target.setVersion(org.snmp4j.mp.SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new org.snmp4j.smi.VariableBinding(new OID("1.3.6.1.2.1.1.5.0")));
            pdu.add(new org.snmp4j.smi.VariableBinding(new OID("1.3.6.1.2.1.1.1.0")));
            pdu.setType(PDU.GET);

            org.snmp4j.event.ResponseEvent event = snmp.get(pdu, target);
            if (event != null && event.getResponse() != null) {
                String sysName = null;
                String sysDescr = null;
                for (org.snmp4j.smi.VariableBinding vb : event.getResponse().getVariableBindings()) {
                    if (vb.getOid().toString().equals("1.3.6.1.2.1.1.5.0")) {
                        sysName = vb.getVariable().toString();
                    } else if (vb.getOid().toString().equals("1.3.6.1.2.1.1.1.0")) {
                        sysDescr = vb.getVariable().toString();
                    }
                }
                if (sysName != null || sysDescr != null) {
                    var deviceOpt = deviceService.getDeviceByIp(ipAddress);
                    if (deviceOpt.isPresent()) {
                        var d = deviceOpt.get();
                        if (sysName != null && (d.getHostname() == null || "Unknown".equals(d.getHostname()))) {
                            d.setHostname(sysName);
                            d.setHostnameSource("SNMP");
                        }
                        if (sysDescr != null && (d.getOperatingSystem() == null || "Unknown".equals(d.getOperatingSystem()))) {
                            d.setOperatingSystem(sysDescr);
                        }
                        deviceService.saveDevice(d);
                    }
                    snmp.close();
                    transport.close();
                    return true;
                }
            }
            snmp.close();
            transport.close();
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Best-effort per-device traffic counters via SNMP.
     * Sums IF-MIB counters across interfaces: ifInOctets/ifOutOctets.
     */
    public SnmpOctetCounters readIfMibOctetsSum(String ipAddress, String community) {
        TransportMapping<UdpAddress> transport = null;
        Snmp snmp = null;
        try {
            Address targetAddress = GenericAddress.parse("udp:" + ipAddress + "/161");
            transport = new DefaultUdpTransportMapping();
            transport.listen();
            snmp = new Snmp(transport);

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setRetries(1);
            target.setTimeout(1500);
            target.setVersion(org.snmp4j.mp.SnmpConstants.version2c);

            long inSum = walkAndSum(snmp, target, new OID("1.3.6.1.2.1.2.2.1.10")); // ifInOctets
            long outSum = walkAndSum(snmp, target, new OID("1.3.6.1.2.1.2.2.1.16")); // ifOutOctets
            return new SnmpOctetCounters(inSum, outSum);
        } catch (Exception e) {
            return null;
        } finally {
            try { if (snmp != null) snmp.close(); } catch (Exception ignored) {}
            try { if (transport != null) transport.close(); } catch (Exception ignored) {}
        }
    }

    private long walkAndSum(Snmp snmp, CommunityTarget target, OID baseOid) throws Exception {
        long sum = 0L;
        OID currentOid = baseOid;
        for (int i = 0; i < 64; i++) { // safety cap
            PDU pdu = new PDU();
            pdu.add(new org.snmp4j.smi.VariableBinding(currentOid));
            pdu.setType(PDU.GETNEXT);

            org.snmp4j.event.ResponseEvent event = snmp.getNext(pdu, target);
            if (event == null || event.getResponse() == null || event.getResponse().getVariableBindings().isEmpty()) {
                break;
            }

            var vb = event.getResponse().getVariableBindings().get(0);
            OID nextOid = vb.getOid();
            if (nextOid == null || !nextOid.startsWith(baseOid)) break;

            String v = vb.getVariable().toString();
            try {
                sum += Long.parseLong(v);
            } catch (NumberFormatException ignored) {
                // skip non-numeric
            }
            currentOid = nextOid;
        }
        return sum;
    }
}
