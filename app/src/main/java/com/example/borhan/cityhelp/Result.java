package com.example.borhan.cityhelp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class Result extends AppCompatActivity {

    //for listView
    //private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView result = (TextView) findViewById(R.id.result);
        //ListView lv = (ListView) findViewById(R.id.listView);

        Intent intent = getIntent();
        Bundle bundle = getIntent().getExtras();

        //ArrayList<String> added1 = intent.getStringArrayListExtra("added1");
        final ArrayList<placesNode> placesList = intent.getParcelableArrayListExtra("placesList1");
        //ArrayList<String> added1 = (ArrayList<String>) bundle.getSerializable("added1");

        //Toast.makeText(getApplication().getBaseContext(),placesList.get(1).getName(), Toast.LENGTH_SHORT).show();


        //for listView
        //lv.setAdapter(adapter);
        //adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_result, added1);


        if (intent != null) {
            result.setText("");

            for (int i = 0; i < placesList.size(); i++) {
                result.append((i + 1) + ": ");
                result.append(placesList.get(i).getName() + "\n");
                result.append(placesList.get(i).getAddress() + "\n");
                result.append(placesList.get(i).getRating() + "\n");
                result.append("\n");


            }
        }


        Button nextBtn = (Button) findViewById(R.id.nextBtn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(Result.this, Direction.class);
                intent.putParcelableArrayListExtra("placesList",placesList);

                //intent.putExtras(bundle);
                //intent.putExtra("added", added);
                startActivity(intent);
            }
        });

    }
}