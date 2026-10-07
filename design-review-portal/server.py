#!/usr/bin/env python3
"""
Zero-dependency HTTP server for the Astra Launcher Visual Review Board & 40-Frame OS Simulator.
Serves design-review-portal/index.html and /wallpapers/* on 0.0.0.0:3000.
"""
import http.server
import os
import socketserver

PORT = int(os.environ.get("PORT", "3000"))
ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
PORTAL_DIR = os.path.abspath(os.path.dirname(__file__))
WALLPAPERS_DIR = os.path.join(ROOT_DIR, "design", "wallpapers")


class AstraReviewHandler(http.server.SimpleHTTPRequestHandler):
    def translate_path(self, path):
        clean = path.split("?", 1)[0].split("#", 1)[0]
        if clean.startswith("/wallpapers/"):
            fname = os.path.basename(clean)
            return os.path.join(WALLPAPERS_DIR, fname)
        if clean == "/" or clean == "/index.html":
            return os.path.join(PORTAL_DIR, "index.html")
        return os.path.join(PORTAL_DIR, clean.lstrip("/"))

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Cache-Control", "no-cache")
        super().end_headers()


if __name__ == "__main__":
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("0.0.0.0", PORT), AstraReviewHandler) as httpd:
        print(f"Astra Visual Review Portal listening on http://0.0.0.0:{PORT}", flush=True)
        httpd.serve_forever()
