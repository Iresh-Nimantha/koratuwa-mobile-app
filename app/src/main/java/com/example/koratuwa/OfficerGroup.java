package com.example.koratuwa;

import java.util.List;

public class OfficerGroup {
    private String district;
    private List<Officer> officers;
    private boolean expanded = false; // track expansion state

    public OfficerGroup() {}

    public OfficerGroup(String district, List<Officer> officers) {
        this.district = district;
        this.officers = officers;
    }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public List<Officer> getOfficers() { return officers; }
    public void setOfficers(List<Officer> officers) { this.officers = officers; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
}
