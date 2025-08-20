# AI Workflow Prompt System

This directory contains the prompt templates for the AI-assisted development workflow. This approach is designed to minimize repetition and placeholder substitution.

## How to Use This System

### Step 1: Set Feature Context

Begin your AI session by setting the feature context:

```
I want to work on feature "feature-name" with path ".requirements/feature-name/".
```

This tells the AI which feature you're working on and where to save artifacts.

### Step 2: Use Numbered Prompts

Once the context is set, you can use simple numbered prompts:

```
Use prompt #1
```

The AI will:
1. Look up prompt #1 (requirement refinement)
2. Apply the feature context you set earlier
3. Execute the prompt with the proper feature name and paths

## Available Prompts

### Main Workflow Prompts

- **#0**: End-to-end workflow (starts from scratch)
- **#1**: Requirement refinement
- **#2**: Implementation and test planning
- **#3**: Code implementation
- **#4**: Test implementation
- **#5**: Documentation

### Specialized Task Prompts

- **#11**: Modify existing code
- **#12**: GraphQL API design
- **#13**: Database changes
- **#14**: Integration testing

## Changing Context

To switch to a different feature during your session:

```
I want to switch to feature "other-feature" with path ".requirements/other-feature/".
```

The AI will update its context and use the new feature name for subsequent prompts.
