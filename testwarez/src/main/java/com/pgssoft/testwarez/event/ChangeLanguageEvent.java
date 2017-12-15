package com.pgssoft.testwarez.event;

import java.util.Locale;

/**
 * Created by dpodolak on 27.05.16.
 */
public class ChangeLanguageEvent {
    private Locale locale;

    public ChangeLanguageEvent(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
}
