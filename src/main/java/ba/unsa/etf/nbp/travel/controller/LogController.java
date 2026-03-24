package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.response.LogResponse;
import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import ba.unsa.etf.nbp.travel.security.Role;
import ba.unsa.etf.nbp.travel.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping
    @Role({"ADMIN"})
    public ResponseEntity<PageResponse<LogResponse>> findAll(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String actionName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (nonNull(tableName)) {
            return ResponseEntity.ok(logService.findByTableName(tableName, page, size));
        }
        if (nonNull(actionName)) {
            return ResponseEntity.ok(logService.findByActionName(actionName, page, size));
        }
        return ResponseEntity.ok(logService.findAll(page, size));
    }
}
