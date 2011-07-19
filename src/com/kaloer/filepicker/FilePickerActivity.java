/*
 * Copyright 2011 Anders Kalør
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaloer.filepicker;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FilePickerActivity extends Activity {

	/**
	 * The file path
	 */
	public final static String EXTRA_FILE_PATH = "file_path";

	/**
	 * Save file mode
	 */
	public final static String EXTRA_SAVE_FILE = "save_file";

	/**
	 * Sets whether hidden files should be visible in the list or not
	 */
	public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";

	/**
	 * The allowed file extensions in an ArrayList of Strings
	 */
	public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "accepted_file_extensions";

	/**
	 * The initial directory which will be used if no directory has been sent
	 * with the intent
	 */
	private final static String DEFAULT_INITIAL_DIRECTORY = "/";

	protected File mDirectory;
	protected ArrayList<File> mFiles;
	protected LinearLayout mLinearLayout;
	protected ListView mFileList;
	protected TextView mTVCurrentFolder;
	protected Button mButCancel;
	protected EditText mEditFileName;
	protected FilePickerListAdapter mAdapter;
	protected boolean mShowHiddenFiles = false;
	protected String[] acceptedFileExtensions;

	protected TextView mTVEmptyView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create the main layout
		createView();

		// Cancel button
		mButCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		// Set the view to be shown if the list is empty
		mTVEmptyView = new TextView(this);
		mTVEmptyView.setText("No Files or Folder");
		mFileList.setEmptyView(mTVEmptyView);

		// set onclick listener
		mFileList.setOnItemClickListener(new FilePickListClickListener());

		// Set initial directory
		mDirectory = new File(DEFAULT_INITIAL_DIRECTORY);
		mTVCurrentFolder.setText(mDirectory.getAbsolutePath());

		// Initialize the ArrayList
		mFiles = new ArrayList<File>();

		// Set the ListAdapter
		mAdapter = new FilePickerListAdapter(this, mFiles);
		mFileList.setAdapter(mAdapter);

		// Initialize the extensions array to allow any file extensions
		acceptedFileExtensions = new String[] {};

		// Get intent extras
		if (getIntent().hasExtra(EXTRA_FILE_PATH)) {
			mDirectory = new File(getIntent().getStringExtra(EXTRA_FILE_PATH));
		}
		if (getIntent().hasExtra(EXTRA_SHOW_HIDDEN_FILES)) {
			mShowHiddenFiles = getIntent().getBooleanExtra(
					EXTRA_SHOW_HIDDEN_FILES, false);
		}
		if (getIntent().hasExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS)) {
			ArrayList<String> collection = getIntent().getStringArrayListExtra(
					EXTRA_ACCEPTED_FILE_EXTENSIONS);
			acceptedFileExtensions = (String[]) collection
					.toArray(new String[collection.size()]);
		}
	}

	protected void createView() {

		mLinearLayout = new LinearLayout(this);
		mLinearLayout.setOrientation(LinearLayout.VERTICAL);
		mLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT));

		// current folder textview
		mTVCurrentFolder = new TextView(this);
		mTVCurrentFolder.setTextAppearance(this,
				android.R.attr.textAppearanceSmall);
		mTVCurrentFolder.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		mLinearLayout.addView(mTVCurrentFolder);

		// cancel button
		mButCancel = new Button(this);
		mButCancel.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		mButCancel.setText(android.R.string.cancel);
		mLinearLayout.addView(mButCancel);

		// save filename edit and ok button
		if (getIntent().hasExtra(EXTRA_SAVE_FILE)) {
			Button butOk = new Button(this);
			butOk.setId(666);
			mEditFileName = new EditText(this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1);
			mEditFileName.setLayoutParams(lp);
			mEditFileName.setSingleLine();
			mEditFileName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
			mEditFileName.setNextFocusDownId(butOk.getId());

			butOk.setText(getString(android.R.string.ok));
			butOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent extra = new Intent();
					String sFileName = mEditFileName.getText().toString()
							.trim();
					if (mEditFileName.length() > 0) {
						extra.putExtra(EXTRA_FILE_PATH,
								mDirectory.getAbsolutePath() + "/" + sFileName);
						setResult(RESULT_OK, extra);
						finish();
					}
				}
			});

			LinearLayout layoutFilename = new LinearLayout(this);
			layoutFilename.setOrientation(LinearLayout.HORIZONTAL);
			layoutFilename.addView(mEditFileName);
			layoutFilename.addView(butOk);

			mLinearLayout.addView(layoutFilename, 2);
		}

		// the file list view
		mFileList = new ListView(this);
		mFileList.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT));
		mLinearLayout.addView(mFileList);

		this.setContentView(mLinearLayout);
	}

	@Override
	protected void onResume() {
		refreshFilesList();
		super.onResume();
	}

	/**
	 * Updates the list view to the current directory
	 */
	protected void refreshFilesList() {
		// Clear the files ArrayList
		mFiles.clear();
		mTVCurrentFolder.setText(" " + mDirectory.getAbsolutePath());

		// Set the extension file filter
		ExtensionFilenameFilter filter = new ExtensionFilenameFilter(
				acceptedFileExtensions);

		// Get the files in the directory
		File[] files = mDirectory.listFiles(filter);
		if (files != null && files.length > 0) {
			for (File f : files) {
				if (f.isHidden() && !mShowHiddenFiles) {
					// Don't add the file
					continue;
				}

				// Add the file the ArrayAdapter
				mFiles.add(f);
			}

			Collections.sort(mFiles, new FileComparator());
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mDirectory.getParentFile() != null) {
				// Go to parent directory
				mDirectory = mDirectory.getParentFile();
				refreshFilesList();
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private class FilePickListClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			File newFile = (File) l.getItemAtPosition(position);

			if (newFile.isFile()) {
				// Set result
				Intent extra = new Intent();
				extra.putExtra(EXTRA_FILE_PATH, newFile.getAbsolutePath());
				setResult(RESULT_OK, extra);
				// Finish the activity
				finish();
			} else {
				mDirectory = newFile;
				// Update the files list
				refreshFilesList();
			}

			// super.onListItemClick(l, v, position, id);
		}
	}

	/**
	 * Converts the given dp to pixels.
	 * 
	 * @param dp
	 *            unit to convert.
	 * @return Pixels for the given dp.
	 */
	private int getDP(float dp) {
		DisplayMetrics metrics = getBaseContext().getResources()
				.getDisplayMetrics();
		return (int) (metrics.density * dp + 0.5f);
	}

	private class FilePickerListAdapter extends BaseAdapter {

		private List<File> mObjects;
		private Context mContext;

		private final static int IMAGEVIEW_ID = 1;
		private final static int TEXTVIEW_ID = 2;

		public FilePickerListAdapter(Context context, List<File> objects) {
			mObjects = objects;
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View row = null;

			if (convertView == null) {
				// create the listview item [image] filename
				LinearLayout ll = new LinearLayout(mContext);
				ll.setOrientation(LinearLayout.HORIZONTAL);

				ImageView imgView = new ImageView(mContext);
				int dp40 = getDP(40);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						dp40, dp40);
				int dp5 = getDP(5);
				lp.setMargins(dp5, dp5, dp5, dp5);
				imgView.setLayoutParams(lp);
				imgView.setScaleType(ScaleType.CENTER_CROP);
				imgView.setId(IMAGEVIEW_ID);
				ll.addView(imgView);

				TextView tv = new TextView(mContext);
				tv.setTextSize(18);
				lp = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT, 1);
				lp.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
				int dp3 = getDP(3);
				lp.setMargins(getDP(10), dp3, dp3, dp3);
				tv.setLayoutParams(lp);
				tv.setSingleLine(true);
				tv.setId(TEXTVIEW_ID);
				ll.addView(tv);

				convertView = ll;
			}
			row = convertView;

			File object = mObjects.get(position);

			ImageView imageView = (ImageView) row.findViewById(IMAGEVIEW_ID);
			TextView textView = (TextView) row.findViewById(TEXTVIEW_ID);

			textView.setText(object.getName());
			if (object.isFile()) {
				// Show the file icon
				imageView.setImageResource(R.drawable.file);
			} else {
				// Show the folder icon
				imageView.setImageResource(R.drawable.folder);
			}

			return row;
		}

		@Override
		public int getCount() {
			return mObjects.size();
		}

		@Override
		public Object getItem(int index) {
			return mObjects.get(index);
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

	}

	private class FileComparator implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			if (f1 == f2) {
				return 0;
			}
			if (f1.isDirectory() && f2.isFile()) {
				// Show directories above files
				return -1;
			}
			if (f1.isFile() && f2.isDirectory()) {
				// Show files below directories
				return 1;
			}
			// Sort the directories alphabetically
			return f1.getName().compareToIgnoreCase(f2.getName());
		}
	}

	private class ExtensionFilenameFilter implements FilenameFilter {
		private String[] mExtensions;

		public ExtensionFilenameFilter(String[] extensions) {
			super();
			mExtensions = extensions;
		}

		@Override
		public boolean accept(File dir, String filename) {
			if (new File(dir, filename).isDirectory()) {
				// Accept all directory names
				return true;
			}
			if (mExtensions != null && mExtensions.length > 0) {
				for (int i = 0; i < mExtensions.length; i++) {
					if (filename.endsWith(mExtensions[i])) {
						// The filename ends with the extension
						return true;
					}
				}
				// The filename did not match any of the extensions
				return false;
			}
			// No extensions has been set. Accept all file extensions.
			return true;
		}
	}
}
