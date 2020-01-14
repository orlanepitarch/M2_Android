package fr.uga.projetannotation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

//affiche bien dans le recyclerView en fonction des données du VM d'annotate et des informtions données à l'adapter depuis Annotate :
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
    }

    @Override
    public int getItemCount() {
        return mContact.size();
    }

    public void setContact(Uri uri) {
        if (!mContact.contains(uri)) {
            mContact.add(uri);
            mContactLive.setValue(mContact);
            this.notifyDataSetChanged();
        }
    }

    public MutableLiveData<Uri> getContactDelete() {
        return mContactDelete;
    }

    public void deleteContact(Uri uri){
        mContactDelete.setValue(uri);
        mContact.remove(uri);
        mContactLive.setValue(mContact);
        this.notifyDataSetChanged();
    }

    public MutableLiveData<List<Uri>> getAllContactsUri() {
        return mContactLive;
    }

    //renvoie le nom du contact en fonction de l'uri afin d'afficher son nom sur l'UI, ce qui est mieux pour l'utilisateur :
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


