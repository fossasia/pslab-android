package io.pslab;

import ir.mirrajabi.searchdialog.core.Searchable;

/**
 * Created by Harsh on 07-05-2018.
 */

public class SearchModel implements Searchable {

    private String mTitle;

    public SearchModel(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }
}
