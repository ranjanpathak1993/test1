package com.example;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<html><head><title>Servlet Hello</title></head><body>");
        out.println("<h1>Hello from Servlet</h1>");
        out.println("<p>Server time: " + LocalDateTime.now() + "</p>");
        out.println("<p><a href=\"/\">Back</a></p>");
        out.println("</body></html>");
    }
}
