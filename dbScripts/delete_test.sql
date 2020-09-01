
delete from test_result where testRunId in
(
    select id from test_run where testParamsId = 40
);
delete from test_run where testParamsId = 40;
delete from test_params where testNo = 40;
