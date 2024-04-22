- update methods for Software Versions Table
  - need to supply manifest names, Nextflow version names
  - add method to work better with newer "topic" channels outputs as per https://github.com/stevekm/nextflow-demos/blob/master/topic-channels-versions/main.nf (https://github.com/stevekm/nf-niceutils/issues/2)
    - this will likely involve using .collect() to gather all process outputs as list/tuple and create Map's of them, then pass Map's to the same methods already used

- update test.nf pipeline to reflect ^ changes
- create method for generating email template, and sending email
- create methods for saving params, metadata, into JSON easily

- refactor all function and class names, etc., so it is cleaner
- see if there's more cruft we can remove