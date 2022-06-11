package hcmute.spkt.truongminhhoang.zaloclone.view.fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Iterables;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hcmute.spkt.truongminhhoang.zaloclone.R;
import hcmute.spkt.truongminhhoang.zaloclone.services.model.Users;
import hcmute.spkt.truongminhhoang.zaloclone.view.adapters.UserFragmentAdapter;
import hcmute.spkt.truongminhhoang.zaloclone.viewModel.DatabaseViewModel;

public class ContactFragment extends Fragment {
    private Context context;
    private DatabaseViewModel databaseViewModel;
    private ArrayList<Users> contactUsers;
    private String currentUserId;
    private RecyclerView recyclerView;
    private UserFragmentAdapter userFragmentAdapter;
    EditText et_search;
    Button btn_sync_contact;

    public ContactFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        init(view);
        syncContact();
        return view;
    }

    private void init(View view) {
        databaseViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(Objects.requireNonNull(getActivity()).getApplication()))
                .get(DatabaseViewModel.class);

        recyclerView = view.findViewById(R.id.user_list_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        contactUsers = new ArrayList<>();
        et_search = view.findViewById(R.id.et_search);
        btn_sync_contact = view.findViewById(R.id.btn_sync_contact);

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (et_search.getText().toString().startsWith(" "))
                    et_search.setText("");
            }
        });

        btn_sync_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncContact();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void searchUsers(String searchText) {
        ArrayList<Users> users = new ArrayList<>();
        for (Users user: contactUsers) {
            if (user.getUsername().contains(searchText) || user.getPhoneNumber().contains(searchText)) {
                users.add(user);
            }
        }
        userFragmentAdapter = new UserFragmentAdapter(users, context, false);
        recyclerView.setAdapter(userFragmentAdapter);
    }

    private void getUserByPhoneNumber(String number) {
        if (!(number.isEmpty() && number.equals(""))) {
            databaseViewModel.fetchSearchedUser(number);

            //Get User by Phone number field
            for (LiveData<DataSnapshot> fetch: databaseViewModel.fetchSearchUser) {
                fetch.observe(this, new Observer<DataSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onChanged(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Users users = (Users) snapshot.getValue(Users.class);
                            assert users != null;
                            if (contactUsers.stream().noneMatch(user -> user.getId().equals(users.getId()))) {
                                contactUsers.add(users);
                            }
                        }
                        userFragmentAdapter = new UserFragmentAdapter(contactUsers, context, false);
                        recyclerView.setAdapter(userFragmentAdapter);
                    }
                });
            }
        }
    }

    private void getUserByListContact(List<String> contacts) {
        contactUsers.clear();
        for (String contact : contacts) {
            getUserByPhoneNumber(contact);
        }
    }

    private void syncContact() {
        List<String> contacts = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.READ_CONTACTS}, 0);
        }

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null ,null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int phoneColIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = phoneColIndex > 0 ? cursor.getString(phoneColIndex) : "";
                contacts.add(phoneNumber);
            }
        }

        getUserByListContact(contacts);
    }
}