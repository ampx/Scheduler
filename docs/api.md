------------------------------

## Update Bookmark Progress

Append a bookmark with new progress entries

**URL** : `/scheduler/query/`
**Method** : `POST`

### URL Parameters

None

### Data Parameters

```json
[
  {"timestamp":{"time":"2018-12-10T13:45:00Z"},"metrics":{"metric0":"metric0Value",...}}}
]
```

### Success Response

```json
{
  "success": true
}
```

### Error Response

Any issues with the request or bookmark service internal errors with result in:

```json
{
  "success": false
}
```

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
BOOKMARK_DATA="[{\"timestamp\":{\"time\":\"`date '+%FT%T.000Z'`\"},\"metrics\":null}]"

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X PUT http://$URL/bookmarks/$BOOKMARK_NAME/bookmark
```

### Notes

------------------------------