package org.zihub.routingservice.utils;

import java.net.UnknownHostException;
import java.util.List;

import org.xbill.DNS.InvalidTypeException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;


public class DNS {


    private SimpleResolver resolv;


    /**
     * Constructor
     *
     * @param timeoutInSeconds the timeout for queries in seconds, 0 means infinite
     */
    public DNS(int timeoutInSeconds, Object obj) {

        if (timeoutInSeconds > 0) {
            try {
                resolv = new SimpleResolver();
                resolv.setTimeout(timeoutInSeconds);
            } catch (UnknownHostException e) {

                System.out.println("dns" + "Failed to find proper DNS host: ");
                //  log.out (Log.WARNING, "dns", "Failed to find proper DNS host: " + e.toString ());

                resolv = null;
            }
        } else {
            resolv = null;
        }
    }

    /**
     * Constructor
     *
     * @param timeoutInSeconds the timeout for queries in seconds
     */
    public DNS(int timeoutInSeconds) {
        this(timeoutInSeconds, null);
    }

    /**
     * Constructor
     */
    public DNS() {
        this(0);
    }

    /**
     * Query a text record for the given domain from DNS
     *
     * @param domain the domain to lookup the DNS for a text record
     * @return the text record content, if found, null otherwise
     */
    @SuppressWarnings("unchecked")
    public String queryText(String domain) {
        String rc = null;
        Record[] r = query(domain, Type.TXT);

        if (r != null) {
            rc = "";
            for (int n = 0; n < r.length; ++n) {
                List<String> answ = ((TXTRecord) r[n]).getStrings();

                for (int m = 0; m < answ.size(); ++m) {
                    String s = answ.get(m);

                    if ((s != null) && (s.length() > 0)) {
                        rc += s;
                    }
                }
            }
        }
        return rc;
    }

    private String typeString(int type) {
        try {
            return Type.string(type);
        } catch (InvalidTypeException e) {
            return "" + type;
        }
    }

    private Record[] query(String q, int type) {
        Lookup l;

        try {
            l = new Lookup(q, type);
            if (resolv != null) {
                l.setResolver(resolv);
            }
        } catch (TextParseException e) {

            // System.err.println("dns", "Failed to parse query \"" + q + "\" (" + typeString (type) + "): " + e.toString ())
            // log.out (Log.INFO, "dns", "Failed to parse query \"" + q + "\" (" + typeString (type) + "): " + e.toString ());

            l = null;
        }
        return l != null ? l.run() : null;
    }
}
