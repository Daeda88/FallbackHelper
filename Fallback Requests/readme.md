Fallback Helper
===============

Introduction
------------
Whenever an API gets rewritten, there will be a timeframe wherein you will be unwilling to fully rely on the new API endpoints. To that end, this sample for an api-handler with fallback support has been written.

This fallback demo will default to a primary (new) api, but depending on any errors received (either http statuses or parseable errors) the old API may automatically be called. The Fallback will also immediately call the old API if failures occur too often, with an exponentially growing window between the attempts to reconnect to the new API.

This implementation is thread-safe.