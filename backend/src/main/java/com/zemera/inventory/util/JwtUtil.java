package com.zemera.inventory.util;



import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.vertx.core.json.JsonObject;
import java.util.Date;


public class JwtUtil {

  private static final String SECRET = "CHANGE_THIS_SECRET";

  public static String generateToken(JsonObject user) {
    return JWT.create()
      .withSubject(user.getString("username"))
      .withClaim("role", user.getString("role"))
      .withClaim("branchId", user.getInteger("branch_id"))
      .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
      .sign(Algorithm.HMAC256(SECRET));
  }



  

}
