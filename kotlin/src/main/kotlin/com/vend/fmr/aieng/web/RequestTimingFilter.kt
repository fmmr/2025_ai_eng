package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.getClientIpAddress
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RequestLoggingFilter : Filter {
    
    private val logger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
    
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        
        val uri = httpRequest.requestURI
        if (uri == "/" || uri.startsWith("/demo/")) {
            val method = httpRequest.method
            val queryString = httpRequest.queryString
            val fullUrl = if (queryString != null) "$uri?$queryString" else uri
            val clientIp = getClientIpAddress(httpRequest)
            
            logger.info("Request: $method $fullUrl | IP: $clientIp")
        }
        
        chain.doFilter(request, response)
    }
}