package fr.uga.projetannotation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {
    private final Context mContext;

    private final LayoutInflater mInflater;
    private List<Uri> mContact = new ArrayList<>(); // Cached copy of contact
    private MutableLiveData<List<Uri>> mContactLive = new MutableLiveData<>();
    private MutableLiveData<Uri> mContactDelete = new MutableLiveData<>();

    ContactListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext =context;
    }
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.contact_item, parent, false);
        //itemView.setOnClickListener();
        return new ContactViewHolder(itemView);
    }

    public void setListContact(List<Uri> contactsUri) {
        mContact.clear();
        mContact.addAll(contactsUri);
        mContactLive.setValue(mContact);
        this.notifyDataSetChanged();
    }


    class ContactViewHolder extends RecyclerView.ViewHolder {
        private final TextView contactTitleItemView;
        private final ImageView btnDelete;
        int position;
        long contactID;

        private ContactViewHolder(View itemView) {
            super(itemView);
            contactTitleItemView = itemView.findViewById(R.id.textView);
            btnDelete = itemView.findViewById(R.id.btnDeleteContact);
        }

        public ItemDetailsLookup.ItemDetails getItemDetails() {
            return new ContactDetails(this);
        }
    }

    static final class ContactDetails extends ItemDetailsLookup.ItemDetails{

        ContactViewHolder selected;

        ContactDetails(ContactViewHolder holder) {
            selected = holder;
        }

        @Override
        public int getPosition() {
            return selected.position;
        }

        @Nullable
        @Override
        public Object getSelectionKey() {
            return selected.contactID;
        }
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        Uri mContactUri = mContact.get(position);
        String contactName = getContactName(mContactUri);
        holder.contactTitleItemView.setText(contactName);
        holder.position=position;
        holder.btnDelete.setOnClickListener(view -> deleteContact(mContactUri));
        //holder.contactID=mCursor.getLong(mCursor.getColumnIndex(ContactsContract.Data._ID));
    }

    @Override
    public int getItemCount() {
        return mContact.size();
    }

    public void setContact(Uri uri) {
        if (!mContact.contains(uri)) {
            mContact.add(uri);
            Log.v("gigig", mContact.toString());
            if(mContactLive.getValue() != null) {
                Log.v("adapter avant", mContactLive.getValue().toString());
            }
            mContactLive.setValue(mContact);
            Log.v("adapter apres", mContactLive.getValue().toString());
            this.notifyDataSetChanged();
        }
    }

    public MutableLiveData<Uri> getContactDelete() {
        return mContactDelete;
    }

    public void deleteContact(Uri uri){
        mContactDelete.setValue(uri);

        mContact.remove(uri);
        Log.v("adapter", mContact.toString());
        mContactLive.setValue(mContact);

        Log.v("del", mContactLive.getValue().toString());
        this.notifyDataSetChanged();
    }

    public MutableLiveData<List<Uri>> getAllContactsUri() {
        return mContactLive;
    }

    public String getContactName(Uri uri) {
        Cursor cursor = null;
        String result = "";
        try {
            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                cursor = mContext.getContentResolver().query(uri,
                        null,
                        null,
                        null,
                        null);
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex
                            (ContactsContract.Data.DISPLAY_NAME));
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.v("contact id", "erreur "+e);
        }
        return result;
    }



}


