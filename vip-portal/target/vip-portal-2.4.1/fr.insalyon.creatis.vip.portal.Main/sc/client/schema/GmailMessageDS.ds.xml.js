isc.DataSource.create({
    allowAdvancedCriteria:true,
    progressiveLoading:true,
    ID:"GmailMessageDS",
    fields:[
        {
            name:"userId",
            type:"text",
            validators:[
            ],
            primaryKey:true
        },
        {
            name:"messageId",
            type:"text",
            validators:[
            ],
            primaryKey:true
        },
        {
            name:"to",
            type:"text",
            validators:[
            ]
        },
        {
            name:"from",
            type:"text",
            validators:[
            ]
        },
        {
            name:"date",
            type:"text",
            validators:[
            ]
        },
        {
            name:"subject",
            type:"text",
            validators:[
            ]
        },
        {
            name:"snippet",
            type:"text",
            validators:[
            ]
        },
        {
            name:"body",
            type:"text",
            validators:[
            ]
        },
        {
            name:"mimeType",
            type:"text",
            validators:[
            ]
        },
        {
            name:"hasAttachments",
            type:"boolean",
            validators:[
            ]
        },
        {
            name:"q",
            type:"text",
            validators:[
            ]
        }
    ]
})
