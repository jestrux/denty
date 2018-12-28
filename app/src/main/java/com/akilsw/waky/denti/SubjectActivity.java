package com.akilsw.waky.denti;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubjectActivity extends AppCompatActivity {

    @BindView(R.id.subjectName)
    TextView subjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        ButterKnife.bind(this);
        Bundle b = getIntent().getExtras();

        if( b != null){
            subjectName.setText(b.getString("name"));
        }
    }
}
