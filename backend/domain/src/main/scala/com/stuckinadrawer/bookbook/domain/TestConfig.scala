package com.stuckinadrawer.bookbook.domain

case class PostgresConfig(
    driver: String,
    url: String,
    user: String,
    pass: String,
)

case class ServiceConf(
    postgres: PostgresConfig
)
