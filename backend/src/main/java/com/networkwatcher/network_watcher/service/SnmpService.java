package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.Device;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
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
}
