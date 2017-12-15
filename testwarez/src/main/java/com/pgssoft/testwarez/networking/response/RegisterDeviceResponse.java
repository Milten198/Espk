package com.pgssoft.testwarez.networking.response;

import java.util.List;

/**
 * Created by dpodolak on 06.06.16.
 */
public class RegisterDeviceResponse {

    private String status;

    private String message;

    private List<Error> errors;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public static class Error{

        private String message;

        private int conference;

        public String getMessage() {
            return message;
        }

        public int getConference() {
            return conference;
        }

        @Override
        public String toString() {
            return String.format("Conference %d not added", conference);
        }
    }
}
