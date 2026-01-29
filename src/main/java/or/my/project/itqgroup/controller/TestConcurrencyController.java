package or.my.project.itqgroup.controller;

import lombok.RequiredArgsConstructor;
import or.my.project.itqgroup.dto.response.ConcurrencyTestResponse;
import or.my.project.itqgroup.service.ConcurrencyTestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping()
@RequiredArgsConstructor
public class TestConcurrencyController {

    private final ConcurrencyTestService concurrencyTestService;

    @PostMapping("/concurrency-test")
    public ConcurrencyTestResponse testConcurrency(
            @RequestParam Long documentId,
            @RequestParam int threads,
            @RequestParam int attempts
    ) {
        return concurrencyTestService.runTest(documentId, threads, attempts);
    }
}
