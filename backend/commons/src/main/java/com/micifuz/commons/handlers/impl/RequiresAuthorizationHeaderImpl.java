package com.micifuz.commons.handlers.impl;

import java.util.Base64;

import com.micifuz.commons.handlers.RequiresAuthorizationHeader;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;

public class RequiresAuthorizationHeaderImpl implements RequiresAuthorizationHeader {

    @Override
    public void handle(RoutingContext ctx) {
        final String authorization = ctx.request().headers().get(AUTHORIZATION);
        if (authorization != null && authorization.toLowerCase().startsWith(BEARER.toLowerCase())) {
            if (authorization.substring(BEARER.length()).trim().isEmpty()) {
                ctx.fail(new HttpException(401, "Missing Authorization Bearer header"));
            }
        } else {
            ctx.fail(new HttpException(401, "Missing Authorization Bearer header"));
        }
        // Load user principal with token payload
        ctx.setUser(User.create(decodePayloadBearer(authorization)));
        ctx.next();
    }

    private JsonObject decodePayloadBearer(final String authorization) {
        String base64Bearer = authorization.substring(BEARER.length()).trim();
        String[] bearerChunks = base64Bearer.split("\\.");
        var payload = new String(Base64.getDecoder().decode(bearerChunks[1]));
        return new JsonObject(payload);
    }

}
