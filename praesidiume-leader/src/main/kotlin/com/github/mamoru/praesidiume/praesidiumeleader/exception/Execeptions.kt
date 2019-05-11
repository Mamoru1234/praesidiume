package com.github.mamoru.praesidiume.praesidiumeleader.exception

import java.lang.RuntimeException

class ClientException(override val message: String): RuntimeException(message)
