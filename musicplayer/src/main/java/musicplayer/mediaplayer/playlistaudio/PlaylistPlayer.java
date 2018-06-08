package musicplayer.mediaplayer.playlistaudio;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PlaylistPlayer extends AsyncTask<String, Void, SparseArray<PlayMusic>> {

    private final static boolean CACHING = true;

    static boolean LOGGING = false;

    private final static String LOG_TAG = "PlaylistPlayer";
    private final static String CACHE_FILE_NAME = "decipher_js_funct";
    private final static int DASH_PARSE_RETRIES = 5;

    private Context context;
    private String videoID;
    private PlayAudio videoMeta;
    private boolean kapsamak = true;
    private boolean useHttp = false;
    private boolean parseDashManifest = false;

    private static final String pyerd= "utu";
    private volatile String decipheredSignature;

    private static String decipherJsFileName;
    private static String decipherFunctions;
    private static String decipherFunctionName;

    private final Lock lock = new ReentrantLock();
    private final Condition jsExecuting = lock.newCondition();

    private static final String ye= "o"+pyerd+"b";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";
    private static final String STREAM_MAP_STRING = "url_encoded_fmt_stream_map";

    private static final Pattern patmmPageLink = Pattern.compile("(http|https)://(www\\.|m.|)y"+ye+"e\\.com/watch\\?v=(.+?)( |\\z|&)");
    private static final Pattern patmmShortLink = Pattern.compile("(http|https)://(www\\.|)yo"+pyerd+".be/(.+?)( |\\z|&)");

    private static final Pattern patDashManifest1 = Pattern.compile("dashmpd=(.+?)(&|\\z)");
    private static final Pattern patDashManifest2 = Pattern.compile("\"dashmpd\":\"(.+?)\"");
    private static final Pattern patDashManifestEncSig = Pattern.compile("/s/([0-9A-F|.]{10,}?)(/|\\z)");

    private static final Pattern patTitle = Pattern.compile("title=(.*?)(&|\\z)");
    private static final Pattern patAuthor = Pattern.compile("author=(.+?)(&|\\z)");
    private static final Pattern patChannelId = Pattern.compile("ucid=(.+?)(&|\\z)");
    private static final Pattern patLength = Pattern.compile("length_seconds=(\\d+?)(&|\\z)");
    private static final Pattern patViewCount = Pattern.compile("view_count=(\\d+?)(&|\\z)");

    private static final Pattern patHlsvp = Pattern.compile("hlsvp=(.+?)(&|\\z)");
    private static final Pattern patHlsItag = Pattern.compile("/itag/(\\d+?)/");

    private static final Pattern patItag = Pattern.compile("itag=([0-9]+?)([&,])");
    private static final Pattern patEncSig = Pattern.compile("s=([0-9A-F|.]{10,}?)([&,\"])");
    private static final Pattern patIsSigEnc = Pattern.compile("s%3D([0-9A-F|.]{10,}?)%26");
    private static final Pattern patUrl = Pattern.compile("url=(.+?)([&,])");

    private static final Pattern patVariableFunction = Pattern.compile("([{; =])([a-zA-Z$][a-zA-Z0-9$]{0,2})\\.([a-zA-Z$][a-zA-Z0-9$]{0,2})\\(");
    private static final Pattern patFunction = Pattern.compile("([{; =])([a-zA-Z$_][a-zA-Z0-9$]{0,2})\\(");
    private static final Pattern patDecryptionJsFile = Pattern.compile("jsbin\\\\/(player-(.+?).js)");
    private static final Pattern patSignatureDecFunction = Pattern.compile("\"signature\",(.{1,3}?)\\(.{1,10}?\\)");

    private static final SparseArray<Player> FORMAT_MAP = new SparseArray<>();

    static {

        FORMAT_MAP.put(17, new Player(17, "3gp", 144, Player.VCodec.MPEG4, Player.ACodec.AAC, 24, false));
        FORMAT_MAP.put(36, new Player(36, "3gp", 240, Player.VCodec.MPEG4, Player.ACodec.AAC, 32, false));
        FORMAT_MAP.put(5, new Player(5, "flv", 240, Player.VCodec.H263, Player.ACodec.MP3, 64, false));
        FORMAT_MAP.put(43, new Player(43, "webm", 360, Player.VCodec.VP8, Player.ACodec.VORBIS, 128, false));
        FORMAT_MAP.put(18, new Player(18, "mp4", 360, Player.VCodec.H264, Player.ACodec.AAC, 96, false));
        FORMAT_MAP.put(22, new Player(22, "mp4", 720, Player.VCodec.H264, Player.ACodec.AAC, 192, false));

        // Dash Video
        FORMAT_MAP.put(160, new Player(160, "mp4", 144, Player.VCodec.H264, Player.ACodec.NONE, true));
        FORMAT_MAP.put(133, new Player(133, "mp4", 240, Player.VCodec.H264, Player.ACodec.NONE, true));
        FORMAT_MAP.put(134, new Player(134, "mp4", 360, Player.VCodec.H264, Player.ACodec.NONE, true));
        FORMAT_MAP.put(135, new Player(135, "mp4", 480, Player.VCodec.H264, Player.ACodec.NONE, true));
        FORMAT_MAP.put(136, new Player(136, "mp4", 720, Player.VCodec.H264, Player.ACodec.NONE, true));
        FORMAT_MAP.put(137, new Player(137, "mp4", 1080, Player.VCodec.H264, Player.ACodec.NONE, true));
        FORMAT_MAP.put(264, new Player(264, "mp4", 1440, Player.VCodec.H264, Player.ACodec.NONE, true));
        FORMAT_MAP.put(266, new Player(266, "mp4", 2160, Player.VCodec.H264, Player.ACodec.NONE, true));

        FORMAT_MAP.put(298, new Player(298, "mp4", 720, Player.VCodec.H264, 60, Player.ACodec.NONE, true));
        FORMAT_MAP.put(299, new Player(299, "mp4", 1080, Player.VCodec.H264, 60, Player.ACodec.NONE, true));

        // Dash Audio
        FORMAT_MAP.put(140, new Player(140, "m4a", Player.VCodec.NONE, Player.ACodec.AAC, 128, true));
        FORMAT_MAP.put(141, new Player(141, "m4a", Player.VCodec.NONE, Player.ACodec.AAC, 256, true));

        // WEBM Dash Video
        FORMAT_MAP.put(278, new Player(278, "webm", 144, Player.VCodec.VP9, Player.ACodec.NONE, true));
        FORMAT_MAP.put(242, new Player(242, "webm", 240, Player.VCodec.VP9, Player.ACodec.NONE, true));
        FORMAT_MAP.put(243, new Player(243, "webm", 360, Player.VCodec.VP9, Player.ACodec.NONE, true));
        FORMAT_MAP.put(244, new Player(244, "webm", 480, Player.VCodec.VP9, Player.ACodec.NONE, true));
        FORMAT_MAP.put(247, new Player(247, "webm", 720, Player.VCodec.VP9, Player.ACodec.NONE, true));
        FORMAT_MAP.put(248, new Player(248, "webm", 1080, Player.VCodec.VP9, Player.ACodec.NONE, true));
        FORMAT_MAP.put(271, new Player(271, "webm", 1440, Player.VCodec.VP9, Player.ACodec.NONE, true));
        FORMAT_MAP.put(313, new Player(313, "webm", 2160, Player.VCodec.VP9, Player.ACodec.NONE, true));

        FORMAT_MAP.put(302, new Player(302, "webm", 720, Player.VCodec.VP9, 60, Player.ACodec.NONE, true));
        FORMAT_MAP.put(308, new Player(308, "webm", 1440, Player.VCodec.VP9, 60, Player.ACodec.NONE, true));
        FORMAT_MAP.put(303, new Player(303, "webm", 1080, Player.VCodec.VP9, 60, Player.ACodec.NONE, true));
        FORMAT_MAP.put(315, new Player(315, "webm", 2160, Player.VCodec.VP9, 60, Player.ACodec.NONE, true));

        // WEBM Dash Audio
        FORMAT_MAP.put(171, new Player(171, "webm", Player.VCodec.NONE, Player.ACodec.VORBIS, 128, true));

        FORMAT_MAP.put(249, new Player(249, "webm", Player.VCodec.NONE, Player.ACodec.OPUS, 48, true));
        FORMAT_MAP.put(250, new Player(250, "webm", Player.VCodec.NONE, Player.ACodec.OPUS, 64, true));
        FORMAT_MAP.put(251, new Player(251, "webm", Player.VCodec.NONE, Player.ACodec.OPUS, 160, true));

        // HLS Live Stream
        FORMAT_MAP.put(91, new Player(91, "mp4", 144 , Player.VCodec.H264, Player.ACodec.AAC, 48, false, true));
        FORMAT_MAP.put(92, new Player(92, "mp4", 240 , Player.VCodec.H264, Player.ACodec.AAC, 48, false, true));
        FORMAT_MAP.put(93, new Player(93, "mp4", 360 , Player.VCodec.H264, Player.ACodec.AAC, 128, false, true));
        FORMAT_MAP.put(94, new Player(94, "mp4", 480 , Player.VCodec.H264, Player.ACodec.AAC, 128, false, true));
        FORMAT_MAP.put(95, new Player(95, "mp4", 720 , Player.VCodec.H264, Player.ACodec.AAC, 256, false, true));
        FORMAT_MAP.put(96, new Player(96, "mp4", 1080 , Player.VCodec.H264, Player.ACodec.AAC, 256, false, true));
    }

    public PlaylistPlayer(Context con) {
        context = con;
    }

    @Override
    protected void onPostExecute(SparseArray<PlayMusic> ytFiles) {
        onExtractionComplete(ytFiles, videoMeta);
    }



    public void extract(String mmLink, boolean parseDashManifest, boolean kapsamak) {
        this.parseDashManifest = parseDashManifest;
        this.kapsamak = kapsamak;
        this.execute(mmLink);
    }

    protected abstract void onExtractionComplete(SparseArray<PlayMusic> ytFiles, PlayAudio videoMeta);

    @Override
    protected SparseArray<PlayMusic> doInBackground(String... params) {
        videoID = null;
        String ytUrl = params[0];
        if (ytUrl == null) {
            return null;
        }
        Matcher mat = patmmPageLink.matcher(ytUrl);
        if (mat.find()) {
            videoID = mat.group(3);
        } else {
            mat = patmmShortLink.matcher(ytUrl);
            if (mat.find()) {
                videoID = mat.group(3);
            } else if (ytUrl.matches("\\p{Graph}+?")) {
                videoID = ytUrl;
            }
        }
        if (videoID != null) {
            try {
                return getStreamUrls();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(LOG_TAG, "Wrong mm link format");
        }
        return null;
    }

    private SparseArray<PlayMusic> getStreamUrls() throws IOException, InterruptedException {

        String ytInfoUrl = (useHttp) ? "http://" : "https://";
        ytInfoUrl += "www.y"+ye+"e.com/get_video_info?video_id=" + videoID + "&eurl="
                + URLEncoder.encode("https://y"+ye+"e.googleapis.com/v/" + videoID, "UTF-8");

        String dashMpdUrl = null;
        String streamMap;
        BufferedReader reader = null;
        URL getUrl = new URL(ytInfoUrl);
        if(LOGGING)
            Log.d(LOG_TAG, "infoUrl: " + ytInfoUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) getUrl.openConnection();
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);
        try {
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            streamMap = reader.readLine();

        } finally {
            if (reader != null)
                reader.close();
            urlConnection.disconnect();
        }
        Matcher mat;
        String faylinadi = null;
        String[] streams;
        SparseArray<String> encSignatures = null;

        parseVideoMeta(streamMap);

        if(videoMeta.isLiveStream()){
            mat = patHlsvp.matcher(streamMap);
            if(mat.find()) {
                String hlsvp = URLDecoder.decode(mat.group(1), "UTF-8");
                SparseArray<PlayMusic> ytFiles = new SparseArray<>();

                getUrl = new URL(hlsvp);
                urlConnection = (HttpURLConnection) getUrl.openConnection();
                urlConnection.setRequestProperty("User-Agent", USER_AGENT);
                try {
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                       if(line.startsWith("https://") || line.startsWith("http://")){
                           mat = patHlsItag.matcher(line);
                           if(mat.find()){
                               int itag = Integer.parseInt(mat.group(1));
                               PlayMusic newFile = new PlayMusic(FORMAT_MAP.get(itag), line);
                               ytFiles.put(itag, newFile);
                           }
                       }
                    }
                } finally {
                    if (reader != null)
                        reader.close();
                    urlConnection.disconnect();
                }

                if (ytFiles.size() == 0) {
                    if (LOGGING)
                        Log.d(LOG_TAG, streamMap);
                    return null;
                }
                return ytFiles;
            }
            return null;
        }

        boolean sigEnc = true;
        if(streamMap != null && streamMap.contains(STREAM_MAP_STRING)){
            String streamMapSub = streamMap.substring(streamMap.indexOf(STREAM_MAP_STRING));
            mat = patIsSigEnc.matcher(streamMapSub);
            if(!mat.find())
                sigEnc = false;
        }


        if (sigEnc) {

            if (CACHING
                    && (decipherJsFileName == null || decipherFunctions == null || decipherFunctionName == null)) {
                readDecipherFunctFromCache();
            }
            getUrl = new URL("https://y"+ye+"e.com/watch?v=" + videoID);
            urlConnection = (HttpURLConnection) getUrl.openConnection();
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            try {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Log.d("line", line);
                    if (line.contains(STREAM_MAP_STRING)) {
                        streamMap = line.replace("\\u0026", "&");
                        break;
                    }
                }
            } finally {
                if (reader != null)
                    reader.close();
                urlConnection.disconnect();
            }
            encSignatures = new SparseArray<>();

            mat = patDecryptionJsFile.matcher(streamMap);
            if (mat.find()) {
                faylinadi = mat.group(1).replace("\\/", "/");
                if (decipherJsFileName == null || !decipherJsFileName.equals(faylinadi)) {
                    decipherFunctions = null;
                    decipherFunctionName = null;
                }
                decipherJsFileName = faylinadi;
            }

            if (parseDashManifest) {
                mat = patDashManifest2.matcher(streamMap);
                if (mat.find()) {
                    dashMpdUrl = mat.group(1).replace("\\/", "/");
                    mat = patDashManifestEncSig.matcher(dashMpdUrl);
                    if (mat.find()) {
                        encSignatures.append(0, mat.group(1));
                    } else {
                        dashMpdUrl = null;
                    }
                }
            }
        } else {
            if (parseDashManifest) {
                mat = patDashManifest1.matcher(streamMap);
                if (mat.find()) {
                    dashMpdUrl = URLDecoder.decode(mat.group(1), "UTF-8");
                }
            }
            streamMap = URLDecoder.decode(streamMap, "UTF-8");
        }

        streams = streamMap.split(",|"+STREAM_MAP_STRING+"|&adaptive_fmts=");
        SparseArray<PlayMusic> ytFiles = new SparseArray<>();
        for (String encStream : streams) {
            encStream = encStream + ",";
            if (!encStream.contains("itag%3D")) {
                continue;
            }
            String stream;
            stream = URLDecoder.decode(encStream, "UTF-8");

            mat = patItag.matcher(stream);
            int itag;
            if (mat.find()) {
                itag = Integer.parseInt(mat.group(1));
                if (LOGGING)
                    Log.d(LOG_TAG, "Itag found:" + itag);
                if (FORMAT_MAP.get(itag) == null) {
                    if (LOGGING)
                        Log.d(LOG_TAG, "Itag not in list:" + itag);
                    continue;
                } else if (!kapsamak && FORMAT_MAP.get(itag).getExt().equals("webm")) {
                    continue;
                }
            } else {
                continue;
            }

            if (faylinadi != null) {
                mat = patEncSig.matcher(stream);
                if (mat.find()) {
                    encSignatures.append(itag, mat.group(1));
                }
            }
            mat = patUrl.matcher(encStream);
            String url = null;
            if (mat.find()) {
                url = mat.group(1);
            }

            if (url != null) {
                Player format = FORMAT_MAP.get(itag);
                String finalUrl = URLDecoder.decode(url, "UTF-8");
                PlayMusic newVideo = new PlayMusic(format, finalUrl);
                ytFiles.put(itag, newVideo);
            }
        }

        if (encSignatures != null) {
            if (LOGGING)
                Log.d(LOG_TAG, "Decipher signatures");
            String signature;
            decipheredSignature = null;
            if (decipherSignature(encSignatures)) {
                lock.lock();
                try {
                    jsExecuting.await(7, TimeUnit.SECONDS);
                } finally {
                    lock.unlock();
                }
            }
            signature = decipheredSignature;
            if (signature == null) {
                return null;
            } else {
                String[] sigs = signature.split("\n");
                for (int i = 0; i < encSignatures.size() && i < sigs.length; i++) {
                    int key = encSignatures.keyAt(i);
                    if (key == 0) {
                        dashMpdUrl = dashMpdUrl.replace("/s/" + encSignatures.get(key), "/signature/" + sigs[i]);
                    } else {
                        String url = ytFiles.get(key).getUrl();
                        url += "&signature=" + sigs[i];
                        PlayMusic newFile = new PlayMusic(FORMAT_MAP.get(key), url);
                        ytFiles.put(key, newFile);
                    }
                }
            }
        }

        if (parseDashManifest && dashMpdUrl != null) {
            for (int i = 0; i < DASH_PARSE_RETRIES; i++) {
                try {
                    // It sometimes fails to connect for no apparent reason. We just retry.
                    parseDashManifest(dashMpdUrl, ytFiles);
                    break;
                } catch (IOException io) {
                    Thread.sleep(5);
                    if (LOGGING)
                        Log.d(LOG_TAG, "Failed to parse dash manifest " + (i + 1));
                }
            }
        }

        if (ytFiles.size() == 0) {
            if (LOGGING)
                Log.d(LOG_TAG, streamMap);
            return null;
        }
        return ytFiles;
    }

    private boolean decipherSignature(final SparseArray<String> encSignatures) throws IOException {
        // Assume the functions don't change that much
        if (decipherFunctionName == null || decipherFunctions == null) {
            String decipherFunctUrl = "https://s.ytimg.com/yts/jsbin/" + decipherJsFileName;

            BufferedReader reader = null;
            String javascriptFile;
            URL url = new URL(decipherFunctUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            try {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder("");
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append(" ");
                }
                javascriptFile = sb.toString();
            } finally {
                if (reader != null)
                    reader.close();
                urlConnection.disconnect();
            }

            if (LOGGING)
                Log.d(LOG_TAG, "Decipher FunctURL: " + decipherFunctUrl);
            Matcher mat = patSignatureDecFunction.matcher(javascriptFile);
            if (mat.find()) {
                decipherFunctionName = mat.group(1);
                if (LOGGING)
                    Log.d(LOG_TAG, "Decipher Functname: " + decipherFunctionName);

                Pattern patMainVariable = Pattern.compile("(var |\\s|,|;)" + decipherFunctionName.replace("$", "\\$") +
                        "(=function\\((.{1,3})\\)\\{)");

                String mainDecipherFunct;

                mat = patMainVariable.matcher(javascriptFile);
                if (mat.find()) {
                    mainDecipherFunct = "var " + decipherFunctionName + mat.group(2);
                } else {
                    Pattern patMainFunction = Pattern.compile("function " + decipherFunctionName.replace("$", "\\$") +
                            "(\\((.{1,3})\\)\\{)");
                    mat = patMainFunction.matcher(javascriptFile);
                    if (!mat.find())
                        return false;
                    mainDecipherFunct = "function " + decipherFunctionName + mat.group(2);
                }

                int startIndex = mat.end();

                for (int braces = 1, i = startIndex; i < javascriptFile.length(); i++) {
                    if (braces == 0 && startIndex + 5 < i) {
                        mainDecipherFunct += javascriptFile.substring(startIndex, i) + ";";
                        break;
                    }
                    if (javascriptFile.charAt(i) == '{')
                        braces++;
                    else if (javascriptFile.charAt(i) == '}')
                        braces--;
                }
                decipherFunctions = mainDecipherFunct;
                // Search the main function for extra functions and variables
                // needed for deciphering
                // Search for variables
                mat = patVariableFunction.matcher(mainDecipherFunct);
                while (mat.find()) {
                    String variableDef = "var " + mat.group(2) + "={";
                    if (decipherFunctions.contains(variableDef)) {
                        continue;
                    }
                    startIndex = javascriptFile.indexOf(variableDef) + variableDef.length();
                    for (int braces = 1, i = startIndex; i < javascriptFile.length(); i++) {
                        if (braces == 0) {
                            decipherFunctions += variableDef + javascriptFile.substring(startIndex, i) + ";";
                            break;
                        }
                        if (javascriptFile.charAt(i) == '{')
                            braces++;
                        else if (javascriptFile.charAt(i) == '}')
                            braces--;
                    }
                }
                // Search for functions
                mat = patFunction.matcher(mainDecipherFunct);
                while (mat.find()) {
                    String functionDef = "function " + mat.group(2) + "(";
                    if (decipherFunctions.contains(functionDef)) {
                        continue;
                    }
                    startIndex = javascriptFile.indexOf(functionDef) + functionDef.length();
                    for (int braces = 0, i = startIndex; i < javascriptFile.length(); i++) {
                        if (braces == 0 && startIndex + 5 < i) {
                            decipherFunctions += functionDef + javascriptFile.substring(startIndex, i) + ";";
                            break;
                        }
                        if (javascriptFile.charAt(i) == '{')
                            braces++;
                        else if (javascriptFile.charAt(i) == '}')
                            braces--;
                    }
                }

                if (LOGGING)
                    Log.d(LOG_TAG, "Decipher Function: " + decipherFunctions);
                decipherViaWebView(encSignatures);
                if (CACHING) {
                    writeDeciperFunctToChache();
                }
            } else {
                return false;
            }
        } else {
            decipherViaWebView(encSignatures);
        }
        return true;
    }

    private void parseDashManifest(String dashMpdUrl, SparseArray<PlayMusic> ytFiles) throws IOException {
        Pattern patBaseUrl = Pattern.compile("<BaseURL yt:contentLength=\"[0-9]+?\">(.+?)</BaseURL>");
        String dashManifest;
        BufferedReader reader = null;
        URL getUrl = new URL(dashMpdUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) getUrl.openConnection();
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);
        try {
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            reader.readLine();
            dashManifest = reader.readLine();

        } finally {
            if (reader != null)
                reader.close();
            urlConnection.disconnect();
        }
        if (dashManifest == null)
            return;
        Matcher mat = patBaseUrl.matcher(dashManifest);
        while (mat.find()) {
            int itag;
            String url = mat.group(1);
            Matcher mat2 = patItag.matcher(url);
            if (mat2.find()) {
                itag = Integer.parseInt(mat2.group(1));
                if (FORMAT_MAP.get(itag) == null)
                    continue;
                if (!kapsamak && FORMAT_MAP.get(itag).getExt().equals("webm"))
                    continue;
            } else {
                continue;
            }
            url = url.replace("&amp;", "&").replace(",", "%2C").
                    replace("mime=audio/", "mime=audio%2F").
                    replace("mime=video/", "mime=video%2F");
            PlayMusic yf = new PlayMusic(FORMAT_MAP.get(itag), url);
            ytFiles.append(itag, yf);
        }

    }

    private void parseVideoMeta(String getVideoInfo) throws UnsupportedEncodingException {
        boolean isLiveStream = false;
        String title = null, author = null, channelId = null;
        long viewCount = 0, length = 0;
        Matcher mat = patTitle.matcher(getVideoInfo);
        if (mat.find()) {
            title = URLDecoder.decode(mat.group(1), "UTF-8");
        }

        mat = patHlsvp.matcher(getVideoInfo);
        if(mat.find())
            isLiveStream = true;

        mat = patAuthor.matcher(getVideoInfo);
        if (mat.find()) {
            author = URLDecoder.decode(mat.group(1), "UTF-8");
        }
        mat = patChannelId.matcher(getVideoInfo);
        if (mat.find()) {
            channelId = mat.group(1);
        }
        mat = patLength.matcher(getVideoInfo);
        if (mat.find()) {
            length = Long.parseLong(mat.group(1));
        }
        mat = patViewCount.matcher(getVideoInfo);
        if (mat.find()) {
            viewCount = Long.parseLong(mat.group(1));
        }
        videoMeta = new PlayAudio(videoID, title, author, channelId, length, viewCount, isLiveStream);

    }

    private void readDecipherFunctFromCache() {
        if (context != null) {
            File cacheFile = new File(context.getCacheDir().getAbsolutePath() + "/" + CACHE_FILE_NAME);

            if (cacheFile.exists() && (System.currentTimeMillis() - cacheFile.lastModified()) < 1209600000) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile), "UTF-8"));
                    decipherJsFileName = reader.readLine();
                    decipherFunctionName = reader.readLine();
                    decipherFunctions = reader.readLine();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    private void writeDeciperFunctToChache() {
        if (context != null) {
            File cacheFile = new File(context.getCacheDir().getAbsolutePath() + "/" + CACHE_FILE_NAME);
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8"));
                writer.write(decipherJsFileName + "\n");
                writer.write(decipherFunctionName + "\n");
                writer.write(decipherFunctions);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void decipherViaWebView(final SparseArray<String> encSignatures) {
        if (context == null) {
            return;
        }

        final StringBuilder stb = new StringBuilder(decipherFunctions + " function decipher(");
        stb.append("){return ");
        for (int i = 0; i < encSignatures.size(); i++) {
            int key = encSignatures.keyAt(i);
            if (i < encSignatures.size() - 1)
                stb.append(decipherFunctionName).append("('").append(encSignatures.get(key)).
                        append("')+\"\\n\"+");
            else
                stb.append(decipherFunctionName).append("('").append(encSignatures.get(key)).
                        append("')");
        }
        stb.append("};decipher();");

        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                new JsEvaluator(context).evaluate(stb.toString(), new JsCallback() {
                    @Override
                    public void onResult(String result) {
                        lock.lock();
                        try {
                            decipheredSignature = result;
                            jsExecuting.signal();
                        } finally {
                            lock.unlock();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        lock.lock();
                        try {
                            if(LOGGING)
                                Log.e(LOG_TAG, errorMessage);
                            jsExecuting.signal();
                        } finally {
                            lock.unlock();
                        }
                    }
                });
            }
        });
    }

}
