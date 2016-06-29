package com.mac.rabbitmq;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rabbitmq.client.ConnectionFactory;

import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {
    private MessageUtility messages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupConnectionFactory();
        messages = new MessageUtility();
        publishToAMQP();
        setupPubButton();
        subscribe();
//        final Handler incomingMessageHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                String message = msg.getData().getString("msg");
//                TextView tv = (TextView) findViewById(R.id.textView);
//                Date now = new Date();
//                SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
//                tv.append(ft.format(now) + ' ' + message + '\n');
//            }
//        };
//        subscribe(incomingMessageHandler);
    }

    void setupPubButton() {
        Button button = (Button) findViewById(R.id.publish);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText et = (EditText) findViewById(R.id.text);
                messages.publishMessage(et.getText().toString());
                et.setText("");
            }
        });
    }


    ConnectionFactory factory = new ConnectionFactory();

    private void setupConnectionFactory() {
        String uri = "amqp://guest:guest@localhost";
        try {
            factory.setAutomaticRecoveryEnabled(false);
           // factory.setUri("amqp://guest:guest@localhost:5672/");
            factory.setUri(uri);
            factory.setHost("localhost");
            factory.setPort(5672);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    void subscribe() {
        Observable<String> receiveObservable = messages.subscribeToMessages(factory);

        receiveObservable.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                String message = s;
                TextView tv = (TextView) findViewById(R.id.textView);
                Date now = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
                tv.append(ft.format(now) + ' ' + message + '\n');
            }
        });

    }

    void publishToAMQP() {
        Observable<String> publishObservable = messages.publishMessagesToAMPG(factory);

        publishObservable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(String message) {

                    Log.d("", "ADDED TO QUEUE: " + message);
                }
            });
    }
}