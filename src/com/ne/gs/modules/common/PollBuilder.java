/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */

package com.ne.gs.modules.common;

import java.util.ArrayList;
import java.util.List;

import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.L10N;
import com.ne.gs.modules.pvpevent.PvpEventScript;

import static java.lang.String.format;

/**
* @author hex1r0
*/
public final class PollBuilder {

    private final List<Tuple2<Integer, Long>> _items = new ArrayList<>(2);
    private String _title = "", _titleColor = "4CB1E5";
    private String _body = "", _bodyColor = "FFFFFF";
    private String _footer = "", _footerColor = "FFFFFF";
    private String _itemCaption = "";
    private int _itemChooseLimit = 0;

    public PollBuilder() {}

    public PollBuilder(String title, String body) {
        _title = title;
        _body = body;
    }

    public PollBuilder setTitle(String title) {
        _title = title;
        return this;
    }

    public PollBuilder setTitle(String title, String color) {
        _title = title;
        _titleColor = color;
        return this;
    }

    public PollBuilder setBody(String body) {
        _body = body;
        return this;
    }

    public PollBuilder setBody(String body, String color) {
        _body = body;
        _bodyColor = color;
        return this;
    }

    public PollBuilder setFooter(String footer) {
        _footer = footer;
        return this;
    }

    public PollBuilder setFooter(String footer, String color) {
        _footer = footer;
        _footerColor = color;
        return this;
    }

//        public PollBuilder addQuestion(String body) {
//            return this;
//        }

    public PollBuilder setItemCaption(String caption) {
        _itemCaption = caption;
        return this;
    }

    public PollBuilder setItemChooseLimit(int limit) {
        _itemChooseLimit = limit;
        return this;
    }

    public PollBuilder addItem(int itemId, long count) {
        _items.add(Tuple2.of(itemId, count));
        return this;
    }

    public String build() {
        // build items
        String items = "";
        for (Tuple2<Integer, Long> i : _items) {
            items += format(TAG_ITEM_ID, i._2, i._1);
        }
        items = format(TAG_REWARD_ITEMS, _itemChooseLimit, items);

        // build template
        String res = TEMPLATE;
        res = res.replace(TITLE, _title).replace(TITLE_COLOR, _titleColor);
        res = res.replace(HEADER, _body).replace(HEADER_COLOR, _bodyColor);
        res = res.replace(FOOTER, _footer).replace(FOOTER_COLOR, _footerColor);

        if (_items.size() > 0) {
            res = res.replace(ITEM_CAPTION, format(TAG_ITEM_CAPTION, _itemCaption));
            res = res.replace(ITEMS, items);
        } else {
            res = res.replace(ITEM_CAPTION, "");
            res = res.replace(ITEMS, "");
        }

        return res;
    }

    @Override
    public String toString() {
        return build();
    }

    public static final class TextBuilder {
        private final StringBuilder _sb = new StringBuilder();
        private L10N.Lang _lang = L10N.Lang.EN;

        public TextBuilder() {}

        public TextBuilder(String text) {
            print(text);
        }

        public TextBuilder(String text, String color) {
            print(text, color);
        }

        public TextBuilder(L10N.Lang lang) {
            setLang(lang);
        }

        public TextBuilder(L10N.Lang lang, String text) {
            this(text);
            setLang(lang);
        }

        public TextBuilder(L10N.Lang lang, String text, String color) {
            this(text, color);
            setLang(lang);
        }

        public TextBuilder setLang(L10N.Lang lang) {
            _lang = lang;
            return this;
        }

        public TextBuilder print(String text) {
            _sb.append(text);
            return this;
        }

        public TextBuilder print(String text, String color) {
            _sb.append("<font color=\"").append(color).append("\">");
            _sb.append(text);
            _sb.append("</font>");
            return this;
        }

        public TextBuilder print(L10N.Translatable msg) {
            print(tr(msg));
            return this;
        }

        public TextBuilder print(L10N.Translatable msg, String color) {
            print(tr(msg), color);
            return this;
        }

        public TextBuilder printf(L10N.Translatable msg, Object... args) {
            print(tr(msg, args));
            return this;
        }

        public TextBuilder println() {
            _sb.append("<br>");
            return this;
        }

        public TextBuilder println(String text) {
            print(text);
            _sb.append("<br>");
            return this;
        }

        public TextBuilder println(String text, String color) {
            print(text, color);
            _sb.append("<br>");
            return this;
        }

        public TextBuilder println(L10N.Translatable msg) {
            print(msg);
            _sb.append("<br>");
            return this;
        }

        public TextBuilder println(L10N.Translatable msg, String color) {
            print(msg, color);
            _sb.append("<br>");
            return this;
        }

        public TextBuilder printlnf(L10N.Translatable msg, Object... args) {
            printf(msg, args);
            _sb.append("<br>");
            return this;
        }

        public String build() {
            return _sb.toString();
        }

        @Override
        public String toString() {
            return build();
        }

        private String tr(L10N.Translatable msg) {
            return L10N.translate(msg, _lang);
        }

        private String tr(L10N.Translatable msg, Object... args) {
            return String.format(L10N.translate(msg, _lang), args);
        }
    }

    private static final String TITLE = "%TITLE%";
    private static final String HEADER = "%HEADER%";
    private static final String TITLE_COLOR = "%TITLE_COLOR%";
    private static final String HEADER_COLOR = "%HEADER_COLOR%";
    private static final String ITEM_CAPTION = "%ITEM_CAPTION%";
    private static final String ITEMS = "%ITEMS%";
    private static final String FOOTER = "%FOOTER%";
    private static final String FOOTER_COLOR = "%FOOTER_COLOR%";

    private static final String TAG_REWARD_ITEMS = "<reward_items multi_count=\"%d\">%s</reward_items>";
    private static final String TAG_ITEM_CAPTION = "<reward_info><![CDATA[%s]]></reward_info>";
    private static final String TAG_ITEM_ID = "<item_id count=\"%d\">%d</item_id>";

    private static final String TEMPLATE = "<poll>" +
        "<poll_title><![CDATA[<font color=\"" + TITLE_COLOR + "\">" + TITLE + "</font>]]></poll_title>" +
        "<poll_introduction><![CDATA[<font color=\"" + HEADER_COLOR + "\">" + HEADER + "</font>]]></poll_introduction>" +
        "<start_date></start_date>" +
        "<end_date></end_date>" +
        "<servers></servers>" +
        "<order_num></order_num>" +
        "<race></race>" +
        "<main_class></main_class>" +
        "<world_id></world_id>" +
        ITEM_CAPTION +
        ITEMS +
        "<level></level>" +
        "<ap></ap>" +
        "<have_item></have_item>" +
        "<quest></quest>" +
        "<legion></legion>" +
//            "<questions><question>" +
//            "<title><![CDATA[%html%]]></title>" +
//            "<select>" +
//            "<input type=\"radio\">%radio%</input>" +
//            "</select>" +
//            "</question></questions>" +
        "<concluding_remarks><![CDATA[<font color=\"" + FOOTER_COLOR + "\">" + FOOTER + "</font>]]></concluding_remarks>" +
        "</poll>";

}
