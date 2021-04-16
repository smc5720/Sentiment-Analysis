package com.example.kaush;

import java.util.HashMap;
import java.util.Map;

public class UserData
{
    private String password;
    private String name;
    private Float emotion1, emotion2;


    public UserData(String name, String password)
    {
        this.password = password;
        this.name = name;
    }

    public UserData(Float emotion1, Float emotion2)
    {
        this.emotion1 = emotion1;
        this.emotion2 = emotion2;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String email)
    {
        this.name = email;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", name);
        result.put("password", password);


        return result;
    }



}
