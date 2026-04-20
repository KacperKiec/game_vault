package io.kk.userinsightsservice.model.mongo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardListsPreview {
    private List<DashboardGamePreview> wishlist;
    private List<DashboardGamePreview> playing;
    private List<DashboardGamePreview> completed;
}
