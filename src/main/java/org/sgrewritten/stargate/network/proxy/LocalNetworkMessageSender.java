package org.sgrewritten.stargate.network.proxy;

import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.proxy.PluginMessageSender;

public class LocalNetworkMessageSender implements PluginMessageSender {
    @Override
    public void sendCreatePortal(RealPortal realPortal) {

    }

    @Override
    public void sendDeletePortal(RealPortal realPortal) {

    }

    @Override
    public void sendRenameNetwork(String newId, String oldId) {

    }

    @Override
    public void sendRenamePortal(String newName, String oldName, Network network) {

    }
}
