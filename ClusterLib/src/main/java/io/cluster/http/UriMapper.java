package io.cluster.http;

import io.cluster.http.core.ControllerManager;
import io.cluster.http.core.HttpResponseUtil;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringPool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UriMapper {

    private static final Logger LOGGER = LogManager.getLogger(UriMapper.class.getName());

    public static final String FAVICON_URI = "/favicon.ico";

    public static FullHttpResponse responseToUri(ChannelHandlerContext ctx, HttpRequest request, String uri) {
        String ipAddress;
        if (request.headers().get("X-Forwarded-For") != null) {
            ipAddress = request.headers().get("X-Forwarded-For");
        } else {
            ipAddress = HttpResponseUtil.getRemoteIP(ctx);
        }
        Object result = handleUri(request, uri);

        if (null == result || result.equals(404) || result.equals(502)) {
            return HttpResponseUtil.theHttpContent(StringPool.BLANK);
        } else {
            return StaticFileHandler.theJSONContent(MethodUtil.toJson(result));
        }
    }

    private static Object handleUri(HttpRequest request, String uri) {
        try {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
            Map<String, List<String>> params = queryStringDecoder.parameters();
            return ControllerManager.invokeUri(queryStringDecoder.path(), params);
        } catch (Exception ex) {
            LOGGER.error("Cannot handle " + uri + " with following error:", ex);
            return 502;
        }
    }

}
