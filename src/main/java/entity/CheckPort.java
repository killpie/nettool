package entity;

import lombok.Data;

@Data
public class CheckPort {

    public static final String port_close="关闭";
    public static final String port_open="开放";
    private String domain;
    private int port;
    private String status;
}
