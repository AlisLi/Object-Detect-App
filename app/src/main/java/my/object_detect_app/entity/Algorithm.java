package my.object_detect_app.entity;

/**
 * 实体类：算法
 * @author lzg
 * @date 2019/4/26 16:16
 * @desc
 */
public class Algorithm {
    private int algorithmNo;    // 算法id
    private String algorithmName;        // 算法名称
    private String url;         // 算法位置
    private String net;         // 算法使用的主干网络

    public Algorithm() {
    }

    public Algorithm(int algorithmNo, String algorithmName, String url, String net) {
        this.algorithmNo = algorithmNo;
        this.algorithmName = algorithmName;
        this.url = url;
        this.net = net;
    }

    public int getAlgorithmNo() {
        return algorithmNo;
    }

    public void setAlgorithmNo(int algorithmNo) {
        this.algorithmNo = algorithmNo;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }

    @Override
    public String toString() {
        return "Algorithm{" +
                "algorithmNo=" + algorithmNo +
                ", algorithmName='" + algorithmName + '\'' +
                ", url='" + url + '\'' +
                ", net='" + net + '\'' +
                '}';
    }
}
