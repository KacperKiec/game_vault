package io.kk.userinsightsservice.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardListsPreview {
    private List<DashboardGamePreview> wishlist;
    private List<DashboardGamePreview> owned;
    private List<DashboardGamePreview> completed;
}
