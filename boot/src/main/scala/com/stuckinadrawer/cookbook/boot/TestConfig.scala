package com.stuckinadrawer.cookbook.boot

case class PostgresConfig(
    driver: String,
    url: String,
    user: String,
    pass: String,
)

case class HttpConfig(host: String, port: Int, allowedOrigins: Set[String])

case class ServiceConf(
    postgres: PostgresConfig,
    http: HttpConfig
)
