package com.LetucOJ.contest.controller;

import com.LetucOJ.contest.model.contest;
import com.LetucOJ.contest.service.contestService;
import com.LetucOJ.contest.tool.ContestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/contest")
public class contestCtrller {

    @Autowired
    private contestService service;

    @PostMapping("/submit/{problemName}")
    public String submit(@PathVariable("problemName") String problemName, @RequestParam("userFile") MultipartFile userFile) throws Exception {
        return service.submit(problemName, userFile.getBytes());
    }

    @PutMapping("/put")
    public String putContest(@RequestBody ContestRequest request) {
        contest newContest = request.getContest();
        int[] problemIds = request.getProblemIds();
        return service.putContest(newContest, problemIds);
    }

    @DeleteMapping("/delete/id/{id}")
    public String deleteContestById(@PathVariable("id") int id) {
        return service.deleteContestById(id);
    }

    @DeleteMapping ("/delete/name/{name}")
    public String deleteContestByName(@PathVariable("name") String name) {
        return service.deleteContestByName(name);
    }

    @PostMapping("/update")
    public String updateContestById(@RequestBody ContestRequest request) {
        contest updatedContest = request.getContest();
        int[] problemIds = request.getProblemIds();
        return service.updateContest(updatedContest, problemIds);
    }

    @GetMapping("/getInfo/{name}")
    public contest getContestInfo(@PathVariable("name") String name) {
        return service.getContestInfo(name);
    }
}