package example.com.wordbook_201713068;
/*
201713068 이민혁
*/

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import example.com.wordbook_201713068.common.IntentExtra;
import example.com.wordbook_201713068.database.AppDatabase;
import example.com.wordbook_201713068.model.Vocabulary;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;



public class EditVocaSearchActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.edit_english)
    EditText englishEdit;
    @BindView(R.id.edit_means)
    EditText meansEdit;

    private AppDatabase mDb;
    private Disposable mVocaDisposable;

    private DBUpdateTask mDbUpdateTask;

    private int mVocaId;
    private boolean mIsNewVoca = true;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_search_voca);
        ButterKnife.bind(this);

        mVocaId = getIntent().getIntExtra(IntentExtra.VOCA_ID, IntentExtra.VOCA_NULL);
        mIsNewVoca = (mVocaId == IntentExtra.VOCA_NULL);

        setActionBarTitleText();

        mDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                AppDatabase.DATABASE_NAME).build();
        mDbUpdateTask = new DBUpdateTask();
        mVocaDisposable = mDb.vocaDao().findById(mVocaId).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(voca -> setData(voca));
    }

    protected void setActionBarTitleText() {
        if (mIsNewVoca) {
            ActionBarManager.initBackArrowActionbar(this, toolbar, getString(R.string.action_add_voca));
        } else {
            ActionBarManager.initBackArrowActionbar(this, toolbar, getString(R.string.action_edit_voca));
        }
    }

    protected void setData(@Nullable Vocabulary previousVoca) {
        if (previousVoca == null) {
            return;
        }

        englishEdit.setText(previousVoca.getEnglish());
        meansEdit.setText(previousVoca.getMeans());
    }

    @NonNull
    protected Vocabulary collectData() {
        Vocabulary newVoca = new Vocabulary(englishEdit.getText().toString(),
                meansEdit.getText().toString(), new Date().getTime());

        if (!mIsNewVoca) {
            newVoca.setId(mVocaId);
        }

        return newVoca;
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            String Search_eng = englishEdit.getText().toString();
            String url = "https://en.dict.naver.com/#/search?query=" + Search_eng;
            String mean_list = null;
            try {
                Document doc = Jsoup.connect(url).get();
                Elements mean = doc.select(".row ").select(".mean_list");

                for (Element element : mean) {
                    mean_list = element.select("p.mean").text();
                }

               // Message message = handler.obtainMessage();
                //handler.sendMessage(message);// 헨들러를 통해서 메인 스레드 제 신호 전달.
            } catch (IOException e) {
                e.printStackTrace();
            }

            Vocabulary newVoca = new Vocabulary(englishEdit.getText().toString(), mean_list
                    , new Date().getTime());
            mDbUpdateTask.execute(newVoca);
        }
    });

    @OnClick(R.id.button_confirm)
    public void onConfirmBtnClicked() {
        mDbUpdateTask.execute(collectData());
    }

    @OnClick(R.id.button_search_word)
    public void onConfirmBtnSrhClicked() {
        thread.start();
    }

    private void goToVocaListActivity() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVocaDisposable.dispose();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home || id == android.R.id.home) {
            goToVocaListActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goToVocaListActivity();
    }

    private class DBUpdateTask extends AsyncTask<Vocabulary, Void, Void> {
        Disposable vocaDisposable;

        @Override
        protected Void doInBackground(Vocabulary... vocabularies) {
            for (Vocabulary voca : vocabularies) {
                if (mIsNewVoca) {
                    vocaDisposable = mDb.vocaDao().insert(voca).subscribe(value -> {
                        if (value <= 0) {
                            Toast.makeText(EditVocaSearchActivity.this,
                                    getString(R.string.err_voca_add_or_edit_fail), Toast.LENGTH_LONG).show();
                        } else {
                            goToVocaListActivity();
                        }
                    });
                } else {
                    vocaDisposable = mDb.vocaDao().update(voca).subscribe(value -> {
                        if (value <= 0) {
                            Toast.makeText(EditVocaSearchActivity.this,
                                    getString(R.string.err_voca_add_or_edit_fail), Toast.LENGTH_LONG).show();
                        } else {
                            goToVocaListActivity();
                        }
                    });
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            vocaDisposable.dispose();
        }
    }
}
