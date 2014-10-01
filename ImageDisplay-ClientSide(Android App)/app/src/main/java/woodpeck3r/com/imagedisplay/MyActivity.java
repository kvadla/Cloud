package woodpeck3r.com.imagedisplay;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketTimeoutException;


public class MyActivity extends Activity {

    String url;
    String status;
    public ImageLoader imageLoader;
    int loader;
    ImageView imageView;
    EditText et_url;

    private TextView txt; //to display status of the call.

    private static final String METHOD_NAME = "GetImageUrl";

    private static final String NAMESPACE = "http://tempuri.org/";

    private static final String MAIN_REQUEST_URL = "http://teamcloudera.cloudapp.net/MobileCloudService.svc";

    final String SOAP_ACTION = "http://tempuri.org/IMobileCloudService/GetImageUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        imageLoader = new ImageLoader(MyActivity.this);
        et_url = (EditText)findViewById(R.id.et_url);
        Button submitBtn = ( Button)findViewById(R.id.submitBtn);
       /* Button servicebtn = (Button)findViewById(R.id.button);
        txt = (TextView)findViewById(R.id.textView2);*/
        loader = R.drawable.stub;//
        imageView  = (ImageView)findViewById(R.id.imageView);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //url = et_url.getText().toString();
                url="https://teamcloudera.blob.core.windows.net/phoenix1repository1/image1.jpg";
             if(!url.equals(""))
               {
                  // imageLoader.DisplayImage("https://teamcloudera.blob.core.windows.net/centralrepocontainer/image1.jpg", loader, imageView);
                   imageLoader.DisplayImage(url, loader, imageView);
               }

            }
        });
       /* servicebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_url.length() > 0) {
                    callService(et_url.getText().toString());
                } else {
                    txt.setText("value can not be empty.");
                }
            }
        });*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final void callService(final String imagename) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                //CloudActivity ex = new CloudActivity();
                status =calServiceAndGetimageUrl(imagename);
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    public Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    txt.setText(status);
                    break;
            }
            return false;
        }
    });

    private final SoapSerializationEnvelope getSoapSerializationEnvelope(SoapObject request) {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(request);

        return envelope;
    }

    private final HttpTransportSE getHttpTransportSE() {
        HttpTransportSE ht = new HttpTransportSE(Proxy.NO_PROXY,MAIN_REQUEST_URL,60000);
        ht.debug = true;
        ht.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");
        return ht;
    }

    public String calServiceAndGetimageUrl(String imgname) {
        String data = null;
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("location","tempelocation");
        request.addProperty("imagename", imgname);

        SoapSerializationEnvelope envelope = getSoapSerializationEnvelope(request);

        HttpTransportSE ht = getHttpTransportSE();
        try {
            ht.call(SOAP_ACTION, envelope);
            SoapPrimitive resultsString = (SoapPrimitive)envelope.getResponse();
            data = resultsString.toString();

        } catch (SocketTimeoutException t) {
            t.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (Exception q) {
            q.printStackTrace();
        }
        return data;
    }


}
