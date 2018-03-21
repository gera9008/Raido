package com.raido.raido;

public class TestHello {

    private final long id;
    private final String content;

    public TestHello(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
