package io.pivotal.pal.tracker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/time-entries")
public class TimeEntryController {

    private TimeEntryRepository timeEntryRepository;
    private final DistributionSummary timeEntrySummary;
    private final Counter actionCounter;

    public TimeEntryController(
            TimeEntryRepository timeEntryRepository,
            MeterRegistry meterRegistry
    ) {
        this.timeEntryRepository = timeEntryRepository;

        timeEntrySummary = meterRegistry.summary("timeEntry.summary");
        actionCounter = meterRegistry.counter("timeEntry.actionCounter");
    }


    @PostMapping
    public ResponseEntity<TimeEntry> create(@RequestBody TimeEntry timeEntry) {
        TimeEntry timeEntry1 = timeEntryRepository.create(timeEntry);
        actionCounter.increment();
        timeEntrySummary.record(timeEntryRepository.list().size());

        return new ResponseEntity<>(timeEntry1,HttpStatus.CREATED);

        //return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryRepository.create(timeEntry));
    }

    @GetMapping("{id}")
    public ResponseEntity<TimeEntry> read(@PathVariable long id) {
        TimeEntry timeEntry1 = timeEntryRepository.find(id);
        if (timeEntry1 ==null) {
            return new ResponseEntity<>(timeEntry1, HttpStatus.NOT_FOUND);
        }
        else{
            actionCounter.increment();
            return new ResponseEntity<>(timeEntry1,HttpStatus.OK);

        }

    }

    @GetMapping
    public ResponseEntity<List<TimeEntry>> list() {
        actionCounter.increment();
        return new ResponseEntity<>(timeEntryRepository.list(),HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<TimeEntry> update(@PathVariable long id, @RequestBody TimeEntry timeEntry) {

        TimeEntry timeEntry1 = timeEntryRepository.update(id,timeEntry);

        if(timeEntry1 == null)
        return new ResponseEntity<>(timeEntry1,HttpStatus.NOT_FOUND);
        else{
            actionCounter.increment();
            return new ResponseEntity<>(timeEntry1,HttpStatus.OK);

        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable("id") long timeEntryId) {

        timeEntryRepository.delete(timeEntryId);
        actionCounter.increment();
        timeEntrySummary.record(timeEntryRepository.list().size());
        return new ResponseEntity<>(null,HttpStatus.NO_CONTENT);
    }
}
