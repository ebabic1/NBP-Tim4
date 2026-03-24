package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.dto.response.LogResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ba.unsa.etf.nbp.travel.util.PaginationUtil.buildPageResponse;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public PageResponse<LogResponse> findAll(int page, int size) {
        var logs = logRepository.findAll(page, size);
        var total = logRepository.count();
        var content = logs.stream()
                .map(e -> new LogResponse(e.getId(), e.getActionName(), e.getTableName(), e.getDateTime(), e.getDbUser()))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public PageResponse<LogResponse> findByTableName(String tableName, int page, int size) {
        var logs = logRepository.findByTableName(tableName, page, size);
        var total = logRepository.countByTableName(tableName);
        var content = logs.stream()
                .map(e -> new LogResponse(e.getId(), e.getActionName(), e.getTableName(), e.getDateTime(), e.getDbUser()))
                .toList();
        return buildPageResponse(content, page, size, total);
    }

    public PageResponse<LogResponse> findByActionName(String actionName, int page, int size) {
        var logs = logRepository.findByActionName(actionName, page, size);
        var total = logRepository.countByActionName(actionName);
        var content = logs.stream()
                .map(e -> new LogResponse(e.getId(), e.getActionName(), e.getTableName(), e.getDateTime(), e.getDbUser()))
                .toList();
        return buildPageResponse(content, page, size, total);
    }
}
