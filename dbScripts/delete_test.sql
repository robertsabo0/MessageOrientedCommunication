
delete from test_result where testRunId in
(
    select id from test_run where testParamsId = 49
);
delete from test_run where testParamsId = 49;
delete from test_params where testNo = 49;
