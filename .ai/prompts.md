# AI Development Workflow Prompts

This document provides example prompts to guide AI through each phase of the development workflow. Use these prompts as templates when working with AI to implement new features or enhancements.

## Phase 1: Requirement Refinement

```
I need to refine a new feature requirement for our basket service following our AI workflow in .ai/workflow.md.

The high-level requirement is: [brief description of feature]

Please help me refine this into a detailed specification by asking clarifying questions. Once we've gathered enough information, create a specification document following the template in .ai/templates/requirements.md and save it to .requirements/[feature-name]/specification.md.
```

## Phase 2: Implementation Planning

```
Based on the requirement specification in .requirements/[feature-name]/specification.md, I need to create implementation and test plans.

Please analyze the technical feasibility, identify component changes needed, and create two separate plans:

1. A step-by-step implementation plan following the template in .ai/templates/implementation-plan.md. Save this plan to .requirements/[feature-name]/implementation-plan.md.

2. A test plan following the template in .ai/templates/test-plan.md. Save this plan to .requirements/[feature-name]/test-plan.md.

For the implementation plan, focus on high-level steps with minimal code snippets - we'll implement the actual code directly in the main source directories based on this plan, not generate reference code.
```

## Phase 3: Code Implementation

```
I'd like to implement the code for the [feature-name] feature based on the implementation plan in .requirements/[feature-name]/implementation-plan.md.

Please help me implement [specific component or step from the plan] directly in the main source directories. Follow our project's coding standards (use var declarations when type can be inferred, use Optional instead of nested null checks, etc.).

No need to create reference implementations in the .requirements directory - let's implement the actual code directly in the project structure.
```

## Phase 4: Testing Strategy

```
Now that we've implemented the code for [feature-name], I'd like to implement the tests according to the test plan in .requirements/[feature-name]/test-plan.md.

Please help me implement the tests directly in the main test directories. Let's focus on the integration tests first, using our recording-based approach with JSON test cases.

[If applicable] We'll also need to implement some unit tests for the complex business logic in [specific component].
```

## Phase 5: Documentation

```
To complete the [feature-name] feature, I need to update documentation.

Please help me with:
1. Adding proper JavaDoc comments to the code
2. Creating a sequence diagram that shows the flow of the new feature
3. Updating any necessary sections in the README.md, including the ubiquitous language section if needed
```

## Complete End-to-End Example

Here's a comprehensive prompt that would take you through the entire process for a new feature:

```
I want to use our AI-assisted development workflow (as defined in .ai/workflow.md) to implement a new feature for our basket service.

The initial requirement is: "We need to allow customers to save their basket for later."

Let's start with Phase 1: Requirement Refinement. Please ask me clarifying questions to understand this requirement better, and then we'll create a detailed specification using the template in .ai/templates/requirements.md.

Once we complete the specification, we'll move through each phase of the workflow:
1. Create separate implementation and test plans
2. Implement the code directly in the main source directories based on the implementation plan
3. Implement the tests directly in the main test directories based on the test plan
4. Update documentation

Please save the specification, implementation plan, and test plan in the .requirements/saved-basket/ directory with the appropriate structure.
```

## Tips for Effective Prompts

1. **Be Specific About the Phase**: Clearly indicate which phase of the workflow you're working on
2. **Reference the Templates**: Point to the relevant template for the current phase
3. **Specify File Paths**: Indicate exactly where files should be saved
4. **Provide Context**: Reference previous artifacts (e.g., "based on the specification in...")
5. **Set Expectations**: Clarify what you expect from the AI in this interaction
6. **Break Down Complex Tasks**: For code implementation, focus on specific components rather than the entire feature at once

## Feature-Specific Prompts

### For Modifying Existing Code

```
I need to modify the existing [component name] to add [new functionality] as part of the [feature-name] feature.

Please analyze the current implementation at [file path] and suggest changes that would implement this functionality while maintaining the existing patterns and style. I'll implement these changes directly in the main source files.
```

### For API Design

```
I need to design the GraphQL API for the [feature-name] feature according to our specification.

Please help me define:
1. The new types needed
2. Any modifications to existing types
3. The queries and mutations required
4. Sample request/response pairs

Follow our existing GraphQL patterns. I'll implement these changes directly in the main source files.
```

### For Database Changes

```
I need to design the database changes for the [feature-name] feature.

Based on our implementation plan, please create the SQL migration script that would:
1. Create any new tables needed
2. Add any columns to existing tables
3. Create appropriate indexes and constraints

I'll implement this SQL migration script directly in the main source files.
```

### For Integration Testing

```
I need to create integration tests for the [feature-name] feature focusing on the GraphQL API.

Please help me implement integration tests using our recording-based approach for:
- [list key operations]

Let's create the JSON test case files directly in the test-cases/[feature-area]/[operation]/ directory.
