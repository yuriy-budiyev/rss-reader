/*
 * MIT License
 *
 * Copyright (c) 2017 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.rssreader.activity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.budiyev.rssreader.R;
import com.budiyev.rssreader.helper.PreferencesHelper;
import com.budiyev.rssreader.helper.ReaderHelper;
import com.budiyev.rssreader.helper.UrlHelper;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private int mWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String mRssFeedAddress;
    private EditText mRssFeedAddressEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        final CharSequence invalidUrlErrorText = getText(R.string.invalid_url);
        mRssFeedAddressEditor = (EditText) findViewById(R.id.rss_feed_address);
        mRssFeedAddressEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String address = s.toString();
                mRssFeedAddress = address;
                if (TextUtils.isEmpty(address) || UrlHelper.validate(address)) {
                    mRssFeedAddressEditor.setError(null);
                } else {
                    mRssFeedAddressEditor.setError(invalidUrlErrorText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mRssFeedAddressEditor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setResultOk();
                    saveSettings();
                    finish();
                }
                return true;
            }
        });
        Bundle extras = getIntent().getExtras();
        int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID && savedInstanceState != null) {
            widgetId = savedInstanceState.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        mWidgetId = widgetId;
        String url = PreferencesHelper.getUrl(this, widgetId);
        if (!TextUtils.isEmpty(url)) {
            mRssFeedAddressEditor.setText(url);
        }
        showKeyboard();
    }

    private void showKeyboard() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .showSoftInput(mRssFeedAddressEditor, 0);
            }
        }, 250);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            setResultOk();
            saveSettings();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int widgetId = mWidgetId;
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            outState.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        setResultOk();
        saveSettings();
        super.onBackPressed();
    }

    private void setResultOk() {
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId));
    }

    private void saveSettings() {
        int widgetId = mWidgetId;
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            String oldUrl = PreferencesHelper.getUrl(this, widgetId);
            PreferencesHelper.setUrl(this, widgetId, mRssFeedAddress);
            if (!Objects.equals(oldUrl, mRssFeedAddress)) {
                ReaderHelper.updateFeed(this, widgetId);
            }
        }
    }
}
