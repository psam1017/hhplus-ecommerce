package hhplus.ecommerce.server.interfaces.web.common.clientinfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ClientInfoHolder {

    private final ThreadLocal<ClientInfo> local = new ThreadLocal<>();

    public void syncClientInfo(String username, String remoteIp) {
        local.set(new ClientInfo(username, remoteIp));
    }

    public String getUsername() {
        if (local.get() == null) {
            return null;
        }
        return local.get().username();
    }

    public String getRemoteIp() {
        if (local.get() == null) {
            return null;
        }
        return local.get().remoteIp();
    }

    public void releaseClientInfo() {
        local.remove();
    }
}
