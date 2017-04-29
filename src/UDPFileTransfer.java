
import java.io.Serializable;

public class UDPFileTransfer implements Serializable {

    public UDPFileTransfer() {
    }

    private static final long serialVersionUID = 1L;

    private String location_A;
    private String location_B;
    private String name;
    private long size;
    private byte[] content;
    private String update;

    public String getLocation_A() {
        return location_A;
    }

    public void setLocation_A(String location_A) {
        this.location_A = location_A;
    }

    public String getLocation_B() {
        return location_B;
    }

    public void setLocation_B(String location_B) {
        this.location_B = location_B;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }
}

