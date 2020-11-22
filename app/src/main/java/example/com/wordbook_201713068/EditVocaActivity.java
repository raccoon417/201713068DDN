package example.com.wordbook_201713068;

/*
201713068 이민혁
*/

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

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

//단어 생성 액티비티
public class EditVocaActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.edit_english)
    EditText englishEdit;
    @BindView(R.id.edit_means)
    EditText meansEdit;
    //view 생성

    private AppDatabase mDb;
    private Disposable mVocaDisposable;

    private DBUpdateTask mDbUpdateTask;

    private int mVocaId;
    private boolean mIsNewVoca = true;
    //백그라운드 디비 업데이트



    //액티비티 실행시 뷰와 디비 업데이트등 필요 값 선언
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_voca);
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


    // 사용자 입력 단어, 뜻 수집하여 DB 업데이트 함수로 리턴
    @NonNull
    protected Vocabulary collectData() {
        Vocabulary newVoca = new Vocabulary(englishEdit.getText().toString(),
                meansEdit.getText().toString(), new Date().getTime());

        if (!mIsNewVoca) {
            newVoca.setId(mVocaId);
        }

        return newVoca;
    }


    // 확인 버튼 입력시
    @OnClick(R.id.button_confirm)
    public void onConfirmBtnClicked() {
        mDbUpdateTask.execute(collectData());
    }

    //액티비티 종료
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


    // 뒤로가기키 눌렸을 경우 단어리스트로 돌아감
    @Override
    public void onBackPressed() {
        goToVocaListActivity();
    }


    // DB 업데이트
    private class DBUpdateTask extends AsyncTask<Vocabulary, Void, Void> {
        Disposable vocaDisposable;

        @Override
        protected Void doInBackground(Vocabulary... vocabularies) {
            for (Vocabulary voca : vocabularies) {
                if (mIsNewVoca) {
                    vocaDisposable = mDb.vocaDao().insert(voca).subscribe(value -> {
                        if (value <= 0) {
                            Toast.makeText(EditVocaActivity.this,
                                    getString(R.string.err_voca_add_or_edit_fail), Toast.LENGTH_LONG).show();
                        } else {
                            goToVocaListActivity();
                        }
                    });
                } else {
                    vocaDisposable = mDb.vocaDao().update(voca).subscribe(value -> {
                        if (value <= 0) {
                            Toast.makeText(EditVocaActivity.this,
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
