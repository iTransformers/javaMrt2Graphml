import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

/**
 * Created by vasko on 8/27/14.
 */
public class UploadData {
    public static void main(String[] args) throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        String url = "https://192.168.30.53:443/guestFile?id=12891&token=522196ad-2a0e-c349-7eab-6215447ab8d912891";
        String fileName = "/home/vasko/projects/POD/vasil-trial-license.dat";
        File file = new File(fileName);
        long fileSize = file.length();
        long startTime = System.currentTimeMillis();
        uploadData(url, fileName, fileSize);
        System.out.println(System.currentTimeMillis()-startTime);
    }
    private static void uploadData(String urlString, String fileName,long fileSize)
            throws Exception {
        HttpURLConnection conn = null;

        URL urlSt = new URL(urlString);
        conn = (HttpURLConnection) urlSt.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Length", Long.toString(fileSize));
        OutputStream out = conn.getOutputStream();
        InputStream in = new FileInputStream(fileName);
        byte[] buf = new byte[102400];
        int len = 0;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        int returnErrorCode = conn.getResponseCode();
        System.out.println("returnErrorCode = "+returnErrorCode);
        conn.disconnect();
        if (HttpsURLConnection.HTTP_OK != returnErrorCode) {
            throw new Exception("File Upload is unsuccessful");
        }
    }

}
