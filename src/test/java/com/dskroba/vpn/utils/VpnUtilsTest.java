package com.dskroba.vpn.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VpnUtilsTest {
    @Test
    public void getNextIPBasic() {
        assertEquals("10.7.0.2", VpnUtils.getNextPeerIp("""
                [Interface]
                Address = 10.7.0.1/24
                PrivateKey = <server_pr_key>
                ListenPort = 80
                PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE
                PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE
                
                
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.21/32
                # END_PEER ****
                """, "10.7.0.1").get());
    }

    @Test
    public void getNextIP() {
        assertEquals("10.7.0.2", VpnUtils.getNextPeerIp("""
                [Interface]
                Address = 10.7.0.1/24
                PrivateKey = <server_pr_key>
                ListenPort = 80
                PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE
                PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE
                """, "10.7.0.1").get());
    }

    @Test
    public void getNextIPBigFile() {
        assertEquals("10.7.0.3", VpnUtils.getNextPeerIp("""
                [Interface]
                Address = 10.7.0.1/24
                PrivateKey = <server_pr_key>
                ListenPort = 80
                PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE
                PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE
                
                
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.21/32
                # END_PEER ****
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.2/32
                # END_PEER ****
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.4/32
                # END_PEER ****
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.5/32
                # END_PEER ****
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.6/32
                # END_PEER ****
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.7/32
                # END_PEER ****
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.8/32
                # END_PEER ****
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.9/32
                # END_PEER ****
                # BEGIN_PEER ****
                [Peer]
                PublicKey = <maskerd-key>
                PresharedKey = <presh-key>
                AllowedIPs = 10.7.0.10/32
                # END_PEER ****
                """, "10.7.0.1").get());
    }

    @Test
    public void removePeer() {
        assertEquals("""
                        [Interface]
                        Address = 10.7.0.1/24
                        PrivateKey = <server_pr_key>
                        ListenPort = 80
                        PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE
                        PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE
                        
                        """,
                VpnUtils.removePeerBlock("""
                        [Interface]
                        Address = 10.7.0.1/24
                        PrivateKey = <server_pr_key>
                        ListenPort = 80
                        PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE
                        PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE
                        
                        
                        # BEGIN_PEER ****
                        [Peer]
                        PublicKey = <maskerd-key>
                        PresharedKey = <presh-key>
                        AllowedIPs = 10.7.0.21/32
                        # END_PEER ****""", "****"));
    }

    @Test
    public void removePeerTwo() {
        assertEquals("""
                        [Interface]
                        Address = 10.7.0.1/24
                        PrivateKey = <server_pr_key>
                        ListenPort = 80
                        PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE
                        PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE
                        
                        # BEGIN_PEER bla
                        [Peer]
                        PublicKey = <maskerd-key>
                        PresharedKey = <presh-key>
                        AllowedIPs = 10.7.0.10/32
                        # END_PEER bla
                        """,
                VpnUtils.removePeerBlock("""
                        [Interface]
                        Address = 10.7.0.1/24
                        PrivateKey = <server_pr_key>
                        ListenPort = 80
                        PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE
                        PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE
                        
                        # BEGIN_PEER bla
                        [Peer]
                        PublicKey = <maskerd-key>
                        PresharedKey = <presh-key>
                        AllowedIPs = 10.7.0.10/32
                        # END_PEER bla
                        
                        # BEGIN_PEER ****
                        [Peer]
                        PublicKey = <maskerd-key>
                        PresharedKey = <presh-key>
                        AllowedIPs = 10.7.0.21/32
                        # END_PEER ****""", "****"));

        assertEquals("""
                        [Interface]
                        Address = 10.7.0.1/24
                        PrivateKey = <server_pr_key>
                        ListenPort = 80
                        PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE
                        PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE
                        
                        
                        # BEGIN_PEER ****
                        [Peer]
                        PublicKey = <maskerd-key>
                        PresharedKey = <presh-key>
                        AllowedIPs = 10.7.0.21/32
                        # END_PEER ****""",
                VpnUtils.removePeerBlock("""
                        [Interface]
                        Address = 10.7.0.1/24
                        PrivateKey = <server_pr_key>
                        ListenPort = 80
                        PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE
                        PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE
                        
                        # BEGIN_PEER bla
                        [Peer]
                        PublicKey = <maskerd-key>
                        PresharedKey = <presh-key>
                        AllowedIPs = 10.7.0.10/32
                        # END_PEER bla
                        
                        # BEGIN_PEER ****
                        [Peer]
                        PublicKey = <maskerd-key>
                        PresharedKey = <presh-key>
                        AllowedIPs = 10.7.0.21/32
                        # END_PEER ****""", "bla"));
    }
}