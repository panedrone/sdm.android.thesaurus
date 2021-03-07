package sdm.android.thesaurus;

//import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.List;

import sdm.android.thesaurus.dao.ThesaurusDao;
import sdm.android.thesaurus.dto.RelatedWord;
import sdm.android.thesaurus.dto.Word;

public class MainActivity extends Activity {

    public void msgBox(String title, String msg) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setTitle(title);
        dlgAlert.setMessage(msg);
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ViewFlipper flipper1 = findViewById(R.id.flipper1);
        final ListView listView1 = flipper1.findViewById(R.id.listView1);
        DataStoreManager dm = new DataStoreManager(getApplicationContext());
        try {
            dm.openReadableDatabase();
        } catch (Throwable e) {
            // http://stackoverflow.com/questions/13794082/android-print-stack-trace
            // e.printStackTrace() would appear in LogCat as well.
            e.printStackTrace();
            msgBox("Error", e.getMessage());
            return;
        }
        ////////////////////////////////////////////////////////
        final ThesaurusDao dao = dm.createThesaurusDao();
        ////////////////////////////////////////////////////////
        Button b_go = flipper1.findViewById(R.id.button);
        b_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText t = flipper1.findViewById(R.id.editText);
                String key = t.getText() + "%";
                try {
                    List<Word> list = dao.getWordsByKey(key);
                    listView1.setAdapter(new ArrayAdapter<Word>(getBaseContext(),
                            android.R.layout.simple_gallery_item, android.R.id.text1, list) {
                        @Override
                        public View getView(int pos, View view, ViewGroup parent) {
                            if (view == null) {
                                view = View.inflate(getContext(), android.R.layout.simple_gallery_item, null);
                            }
                            Word w = getItem(pos);
                            TextView textView = view.findViewById(android.R.id.text1);
                            textView.setText(w.getWWord());
                            return view;
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                    msgBox("Error", e.getMessage());
                }
            }
        });

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Word word = (Word) listView1.getItemAtPosition(position);
                TextView tv = flipper1.findViewById(R.id.wordTextView);
                assert word != null;
                tv.setText(word.getWWord());
                ////////////////////////////////////////////////
                flipper1.showNext();
                try {
                    List<RelatedWord> list = dao.getRelatedWords(word.getWId());
                    final ListView listView2 = flipper1.findViewById(R.id.listView2);
                    listView2.setAdapter(new ArrayAdapter<RelatedWord>(getBaseContext(),
                            android.R.layout.simple_list_item_2, android.R.id.text1, list) {
                        @Override
                        public View getView(int pos, View view, ViewGroup parent) {
                            if (view == null) {
                                view = View.inflate(getContext(), android.R.layout.simple_list_item_2, null);
                            }
                            RelatedWord rw = getItem(pos);
                            TextView textView1 = view.findViewById(android.R.id.text1);
                            textView1.setText(rw.getRgwWord().toString());
                            String s = "";
                            if (rw.getRgPartOfSpeech() != null) {
                                s += rw.getRgPartOfSpeech() + " ";
                            }
                            if (rw.getRgwNote() != null) {
                                s += rw.getRgwNote();
                            }
                            TextView textView2 = view.findViewById(android.R.id.text2);
                            textView2.setText(s);
                            return view;
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                    msgBox("Error", e.getMessage());
                }
            }
        });

        Button b_back = flipper1.findViewById(R.id.button2);
        b_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipper1.showPrevious();
            }
        });
    }
}
