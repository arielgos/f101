package com.uagrm.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.uagrm.chat.model.Message;
import com.uagrm.chat.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String tag = "UAGRM";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fireStore;
    private FirebaseUser firebaseUser;
    private GoogleSignInClient googleSignInClient;
    private final int RC_SIGN_IN = 100;
    private User user = null;
    private final List<Message> messages = new ArrayList();

    @BindView(R.id.username)
    TextView username;
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.message)
    EditText message;
    @BindView(R.id.send)
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        firebaseAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        username.setOnClickListener(v -> {
            firebaseAuth.signOut();
            googleSignInClient.signOut();
            finishAndRemoveTask();
        });

        send.setOnClickListener(v -> {
            if (!message.getText().toString().isEmpty()) {
                Message messageToSend = new Message();
                messageToSend.setId(user.getId());
                messageToSend.setMessage(message.getText().toString());
                messageToSend.setUser(user.getUser());
                messageToSend.setDate(new Date().getTime());
                fireStore.collection("messages").add(messageToSend);
                message.setText("");
            }
        });

        list.setAdapter(new MessageAdapter(MainActivity.this, messages));
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            loadInterface();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(tag, "signInWithCredential:success");
                        firebaseUser = firebaseAuth.getCurrentUser();
                        loadInterface();
                    } else {
                        Log.w(tag, "signInWithCredential:failure", task.getException());
                    }
                });
    }

    private void loadInterface() {
        Log.d(tag, firebaseUser.getDisplayName());
        user = new User();
        user.setId(firebaseUser.getUid());
        user.setUser(firebaseUser.getDisplayName());
        user.setImage(firebaseUser.getPhotoUrl().toString());
        fireStore.collection("users")
                .document(user.getId())
                .set(user);
        username.setText(user.getUser());

        CollectionReference reference = fireStore.collection("messages");
        reference.orderBy("date").addSnapshotListener((snapshot, error) -> {
            if (snapshot != null && user != null) {
                for (DocumentChange document : snapshot.getDocumentChanges()) {
                    if (document.getType().equals(DocumentChange.Type.ADDED)) {
                        messages.add(document.getDocument().toObject(Message.class));
                        Log.d(tag, messages.toString());
                        ((MessageAdapter) list.getAdapter()).notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public class MessageAdapter extends ArrayAdapter<Message> {

        public MessageAdapter(Context context, List<Message> messages) {
            super(context, 0, messages);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Message message = getItem(position);
            if (message.getId().equals(user.getId())) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item_right, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item, parent, false);
            }
            TextView tvMessage = convertView.findViewById(R.id.message);
            TextView tvUserName = convertView.findViewById(R.id.userName);
            TextView tvDate = convertView.findViewById(R.id.date);
            tvMessage.setText(message.getMessage());
            tvUserName.setText(message.getUser());
            tvDate.setText(Long.toString(message.getDate()));
            return convertView;
        }
    }

}